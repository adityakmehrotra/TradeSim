package com.adityamehrotra.tradesim.market;

import com.adityamehrotra.tradesim.engine.BookLevel;
import com.adityamehrotra.tradesim.engine.Order;
import com.adityamehrotra.tradesim.engine.OrderBook;
import com.adityamehrotra.tradesim.engine.OrderType;
import com.adityamehrotra.tradesim.engine.Side;
import com.adityamehrotra.tradesim.engine.Trade;
import com.adityamehrotra.tradesim.service.PositionService;
import jakarta.annotation.PostConstruct;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Runs the simulated exchange: one order book per symbol, bot liquidity, and the accounting that
 * turns fills into cash and position changes. The books live in memory and are rebuilt on restart.
 * Orders placed by a real account are tracked so their fills settle against a portfolio; bot orders
 * are not.
 */
@Service
public class MarketService {
  private static final long BOT_OWNER = -1;
  private static final int DEPTH = 12;
  private static final int MAX_RECENT_TRADES = 50;
  private static final int MAX_CANDLES = 120;
  private static final int MM_LEVELS = 5;

  private final PositionService positionService;
  private final Map<String, OrderBook> books = new LinkedHashMap<>();
  private final Map<String, MarketState> states = new LinkedHashMap<>();
  private final Map<Long, OrderContext> contexts = new LinkedHashMap<>();
  private final AtomicLong sequence = new AtomicLong(1);
  private final Random random = new Random(7);
  private final Object lock = new Object();

  public MarketService(PositionService positionService) {
    this.positionService = positionService;
  }

  @PostConstruct
  void seed() {
    synchronized (lock) {
      for (Instrument instrument : Instruments.ALL) {
        OrderBook book = new OrderBook(instrument.symbol());
        MarketState state = new MarketState(instrument.referencePriceCents());
        books.put(instrument.symbol(), book);
        states.put(instrument.symbol(), state);
        requote(instrument, book, state);
      }
    }
  }

  public List<InstrumentView> instruments() {
    synchronized (lock) {
      List<InstrumentView> views = new ArrayList<>();
      for (Instrument instrument : Instruments.ALL) {
        views.add(
            new InstrumentView(
                instrument.symbol(), instrument.name(), states.get(instrument.symbol()).lastPrice));
      }
      return views;
    }
  }

  public Quote quote(String symbol) {
    synchronized (lock) {
      Instrument instrument = instrument(symbol);
      OrderBook book = books.get(symbol);
      MarketState state = states.get(symbol);
      return new Quote(
          symbol, instrument.name(), state.lastPrice, book.bestBidCents(), book.bestAskCents());
    }
  }

  public DepthView depth(String symbol) {
    synchronized (lock) {
      OrderBook book = requireBook(symbol);
      return new DepthView(symbol, book.bidLevels(DEPTH), book.askLevels(DEPTH));
    }
  }

  public List<TradeView> recentTrades(String symbol) {
    synchronized (lock) {
      return new ArrayList<>(requireState(symbol).recentTrades);
    }
  }

  public List<Candle> candles(String symbol) {
    synchronized (lock) {
      return new ArrayList<>(requireState(symbol).candles);
    }
  }

  public List<OrderView> openOrders(int accountId) {
    synchronized (lock) {
      List<OrderView> views = new ArrayList<>();
      for (OrderContext context : contexts.values()) {
        if (context.accountId == accountId) {
          views.add(
              new OrderView(
                  context.orderId,
                  context.symbol,
                  context.side.name(),
                  context.type.name(),
                  context.limitPriceCents,
                  context.remaining));
        }
      }
      return views;
    }
  }

  public long lastPrice(String symbol) {
    synchronized (lock) {
      return requireState(symbol).lastPrice;
    }
  }

  /** Places an order for a real account, reserving cash or shares before it reaches the book. */
  public PlaceResult placeOrder(
      int portfolioId,
      int accountId,
      String symbol,
      Side side,
      OrderType type,
      Long limitPriceCents,
      long quantity) {
    synchronized (lock) {
      Instrument instrument = instrument(symbol);
      if (quantity <= 0) {
        throw new IllegalArgumentException("Quantity must be positive");
      }
      OrderBook book = books.get(instrument.symbol());

      long reserveRate = 0;
      long orderQuantity = quantity;

      if (side == Side.BUY) {
        if (type == OrderType.LIMIT) {
          if (limitPriceCents == null || limitPriceCents <= 0) {
            throw new IllegalArgumentException("A limit order needs a positive price");
          }
          reserveRate = limitPriceCents;
          if (!positionService.reserveCash(portfolioId, reserveRate * quantity)) {
            throw new IllegalArgumentException("Not enough cash for this order");
          }
        } else {
          long budget = Math.round(availableCashDollars(portfolioId) * 100);
          long[] affordable = walkAsks(book, budget, quantity);
          orderQuantity = affordable[0];
          if (orderQuantity <= 0) {
            throw new IllegalArgumentException("Not enough cash to buy at the current ask");
          }
          positionService.reserveCash(portfolioId, affordable[1]);
        }
      } else {
        if (type == OrderType.LIMIT && (limitPriceCents == null || limitPriceCents <= 0)) {
          throw new IllegalArgumentException("A limit order needs a positive price");
        }
        if (!positionService.reserveShares(portfolioId, symbol, quantity)) {
          throw new IllegalArgumentException("Not enough shares for this order");
        }
      }

      long orderId = sequence.getAndIncrement();
      long priceForOrder = type == OrderType.LIMIT ? limitPriceCents : 0;
      OrderContext context =
          new OrderContext(
              orderId, portfolioId, accountId, symbol, side, type, reserveRate, orderQuantity);
      contexts.put(orderId, context);

      Order order = new Order(orderId, accountId, side, type, priceForOrder, orderQuantity);
      List<Trade> trades = book.submit(order);
      applyTrades(symbol, trades);

      boolean resting = order.getRemainingQuantity() > 0 && type == OrderType.LIMIT;
      if (!resting) {
        releaseLeftover(context, order.getRemainingQuantity());
        contexts.remove(orderId);
      }

      return new PlaceResult(
          orderId, order.getFilledQuantity(), order.getRemainingQuantity(), resting);
    }
  }

  public boolean cancelOrder(int accountId, long orderId) {
    synchronized (lock) {
      OrderContext context = contexts.get(orderId);
      if (context == null || context.accountId != accountId) {
        return false;
      }
      boolean removed = books.get(context.symbol).cancel(orderId);
      if (removed) {
        releaseLeftover(context, context.remaining);
        contexts.remove(orderId);
      }
      return removed;
    }
  }

  private void applyTrades(String symbol, List<Trade> trades) {
    MarketState state = states.get(symbol);
    for (Trade trade : trades) {
      state.record(trade);

      OrderContext buyer = contexts.get(trade.getBuyOrderId());
      if (buyer != null) {
        long rate = buyer.type == OrderType.LIMIT ? buyer.reserveRate : trade.getPriceCents();
        positionService.applyBuyFill(
            buyer.portfolioId, symbol, trade.getQuantity(), trade.getPriceCents(), rate);
        reduceContext(buyer, trade.getQuantity());
      }

      OrderContext seller = contexts.get(trade.getSellOrderId());
      if (seller != null) {
        positionService.applySellFill(
            seller.portfolioId, symbol, trade.getQuantity(), trade.getPriceCents());
        reduceContext(seller, trade.getQuantity());
      }
    }
  }

  private void reduceContext(OrderContext context, long quantity) {
    context.remaining -= quantity;
    if (context.remaining <= 0) {
      contexts.remove(context.orderId);
    }
  }

  private void releaseLeftover(OrderContext context, long remaining) {
    if (remaining <= 0) {
      return;
    }
    if (context.side == Side.BUY) {
      long rate = context.type == OrderType.LIMIT ? context.reserveRate : 0;
      positionService.releaseCash(context.portfolioId, rate * remaining);
    } else {
      positionService.releaseShares(context.portfolioId, context.symbol, remaining);
    }
  }

  @Scheduled(fixedDelay = 1000)
  public void tick() {
    synchronized (lock) {
      for (Instrument instrument : Instruments.ALL) {
        OrderBook book = books.get(instrument.symbol());
        MarketState state = states.get(instrument.symbol());

        double shockBps =
            random.nextGaussian() * instrument.volatilityBps() + instrument.driftBps();
        state.reference = Math.max(1, Math.round(state.reference * (1 + shockBps / 10000.0)));

        requote(instrument, book, state);

        if (random.nextDouble() < 0.6) {
          Side side = random.nextBoolean() ? Side.BUY : Side.SELL;
          long quantity = 1 + random.nextInt(25);
          Order noise =
              new Order(sequence.getAndIncrement(), BOT_OWNER, side, OrderType.MARKET, 0, quantity);
          applyTrades(instrument.symbol(), book.submit(noise));
        }
      }
    }
  }

  @Scheduled(fixedDelay = 5000)
  public void closeCandle() {
    synchronized (lock) {
      for (MarketState state : states.values()) {
        state.rollCandle();
      }
    }
  }

  private void requote(Instrument instrument, OrderBook book, MarketState state) {
    for (Long id : state.botOrderIds) {
      book.cancel(id);
    }
    state.botOrderIds.clear();

    long reference = state.reference;
    long step = Math.max(1, reference / 1000);
    for (int level = 1; level <= MM_LEVELS; level++) {
      long size = 20 + random.nextInt(80);
      Order bid =
          new Order(
              sequence.getAndIncrement(),
              BOT_OWNER,
              Side.BUY,
              OrderType.LIMIT,
              reference - step * level,
              size);
      Order ask =
          new Order(
              sequence.getAndIncrement(),
              BOT_OWNER,
              Side.SELL,
              OrderType.LIMIT,
              reference + step * level,
              size);
      applyTrades(instrument.symbol(), book.submit(bid));
      applyTrades(instrument.symbol(), book.submit(ask));
      state.botOrderIds.add(bid.getId());
      state.botOrderIds.add(ask.getId());
    }
  }

  private long[] walkAsks(OrderBook book, long budgetCents, long wantQuantity) {
    long quantity = 0;
    long cost = 0;
    long remaining = wantQuantity;
    for (BookLevel level : book.askLevels(1000)) {
      if (remaining <= 0) {
        break;
      }
      long affordable = (budgetCents - cost) / level.priceCents();
      long take = Math.min(Math.min(remaining, level.quantity()), affordable);
      if (take <= 0) {
        break;
      }
      quantity += take;
      cost += take * level.priceCents();
      remaining -= take;
      if (take < level.quantity()) {
        break;
      }
    }
    return new long[] {quantity, cost};
  }

  private double availableCashDollars(int portfolioId) {
    return positionService.availableCash(portfolioId);
  }

  private Instrument instrument(String symbol) {
    return Instruments.ALL.stream()
        .filter(candidate -> candidate.symbol().equals(symbol))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown symbol: " + symbol));
  }

  private OrderBook requireBook(String symbol) {
    OrderBook book = books.get(symbol);
    if (book == null) {
      throw new IllegalArgumentException("Unknown symbol: " + symbol);
    }
    return book;
  }

  private MarketState requireState(String symbol) {
    MarketState state = states.get(symbol);
    if (state == null) {
      throw new IllegalArgumentException("Unknown symbol: " + symbol);
    }
    return state;
  }

  private static final class MarketState {
    private long reference;
    private long lastPrice;
    private long candleOpen;
    private long candleHigh;
    private long candleLow;
    private long candleVolume;
    private final Deque<TradeView> recentTrades = new ArrayDeque<>();
    private final Deque<Candle> candles = new ArrayDeque<>();
    private final List<Long> botOrderIds = new ArrayList<>();

    private MarketState(long reference) {
      this.reference = reference;
      this.lastPrice = reference;
      this.candleOpen = reference;
      this.candleHigh = reference;
      this.candleLow = reference;
    }

    private void record(Trade trade) {
      lastPrice = trade.getPriceCents();
      candleHigh = Math.max(candleHigh, trade.getPriceCents());
      candleLow = Math.min(candleLow, trade.getPriceCents());
      candleVolume += trade.getQuantity();
      recentTrades.addFirst(
          new TradeView(
              trade.getSymbol(),
              trade.getPriceCents(),
              trade.getQuantity(),
              trade.getAggressor().name(),
              System.currentTimeMillis()));
      while (recentTrades.size() > MAX_RECENT_TRADES) {
        recentTrades.removeLast();
      }
    }

    private void rollCandle() {
      candles.addLast(
          new Candle(
              candleOpen,
              candleHigh,
              candleLow,
              lastPrice,
              candleVolume,
              System.currentTimeMillis()));
      while (candles.size() > MAX_CANDLES) {
        candles.removeFirst();
      }
      candleOpen = lastPrice;
      candleHigh = lastPrice;
      candleLow = lastPrice;
      candleVolume = 0;
    }
  }

  private static final class OrderContext {
    private final long orderId;
    private final int portfolioId;
    private final int accountId;
    private final String symbol;
    private final Side side;
    private final OrderType type;
    private final long reserveRate;
    private final long limitPriceCents;
    private long remaining;

    private OrderContext(
        long orderId,
        int portfolioId,
        int accountId,
        String symbol,
        Side side,
        OrderType type,
        long reserveRate,
        long quantity) {
      this.orderId = orderId;
      this.portfolioId = portfolioId;
      this.accountId = accountId;
      this.symbol = symbol;
      this.side = side;
      this.type = type;
      this.reserveRate = reserveRate;
      this.limitPriceCents = reserveRate;
      this.remaining = quantity;
    }
  }

  public record InstrumentView(String symbol, String name, long lastCents) {}

  public record Quote(String symbol, String name, long lastCents, Long bidCents, Long askCents) {}

  public record DepthView(String symbol, List<BookLevel> bids, List<BookLevel> asks) {}

  public record TradeView(
      String symbol, long priceCents, long quantity, String aggressor, long epochMillis) {}

  public record Candle(
      long openCents,
      long highCents,
      long lowCents,
      long closeCents,
      long volume,
      long epochMillis) {}

  public record OrderView(
      long orderId,
      String symbol,
      String side,
      String type,
      long limitPriceCents,
      long remaining) {}

  public record PlaceResult(long orderId, long filled, long remaining, boolean resting) {}
}
