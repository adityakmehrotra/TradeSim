package com.adityamehrotra.tradesim.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Times single operations against a seeded {@link OrderBook}. Run it from the server directory with
 * {@code ./mvnw -Pbench -DskipTests test-compile exec:exec}.
 *
 * <p>The book is seeded with {@code depth} price levels on each side and a few orders per level.
 * Every scenario leaves the book the same shape it found it, so an iteration measures a steady
 * state rather than a book that grows or drains as invocations pile up.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
public class OrderBookBenchmark {
  private static final long TICK = 1;
  private static final long BEST_BID = 9_999;
  private static final long BEST_ASK = 10_001;
  private static final long QUANTITY = 100;
  private static final int ORDERS_PER_LEVEL = 4;
  private static final int MIXED_UNITS = 100;
  private static final long OWNER = 1;

  @Param({"10", "100", "1000"})
  int depth;

  private OrderBook book;
  private long nextId;
  private Order[] seededBids;
  private int replaceCursor;

  private Op[] ring;
  private long[] submittedIds;
  private int ringCursor;

  @Setup(Level.Trial)
  public void buildRing() {
    ring = mixedRing(depth);
    submittedIds = new long[ring.length];
  }

  @Setup(Level.Iteration)
  public void seedBook() {
    book = new OrderBook("BENCH");
    nextId = 0;
    seededBids = new Order[depth * ORDERS_PER_LEVEL];
    for (int level = 0; level < depth; level++) {
      for (int i = 0; i < ORDERS_PER_LEVEL; i++) {
        Order bid = limit(Side.BUY, BEST_BID - level * TICK, QUANTITY);
        book.submit(bid);
        seededBids[level * ORDERS_PER_LEVEL + i] = bid;
        book.submit(limit(Side.SELL, BEST_ASK + level * TICK, QUANTITY));
      }
    }
    replaceCursor = 0;
    ringCursor = 0;
  }

  /** A bid joins the queue one tick below the touch and is pulled again. */
  @Benchmark
  public void restThenCancel(Blackhole bh) {
    Order bid = limit(Side.BUY, BEST_BID - TICK, QUANTITY);
    bh.consume(book.submit(bid));
    bh.consume(book.cancel(bid.getId()));
  }

  /** A buy takes out the whole best ask level, then the level is quoted again. */
  @Benchmark
  public void crossOneLevel(Blackhole bh) {
    bh.consume(book.submit(limit(Side.BUY, BEST_ASK, ORDERS_PER_LEVEL * QUANTITY)));
    for (int i = 0; i < ORDERS_PER_LEVEL; i++) {
      bh.consume(book.submit(limit(Side.SELL, BEST_ASK, QUANTITY)));
    }
  }

  /** A seeded bid is cancelled and requoted at the same price, walking every level in turn. */
  @Benchmark
  public void cancelAndReplace(Blackhole bh) {
    int slot = replaceCursor;
    replaceCursor = slot + 1 == seededBids.length ? 0 : slot + 1;
    Order old = seededBids[slot];
    bh.consume(book.cancel(old.getId()));
    Order replacement = limit(Side.BUY, old.getLimitPriceCents(), QUANTITY);
    bh.consume(book.submit(replacement));
    seededBids[slot] = replacement;
  }

  /** One step of the pre-generated ring: seventy percent rests, twenty crosses, ten cancels. */
  @Benchmark
  public void mixed(Blackhole bh) {
    int slot = ringCursor;
    ringCursor = slot + 1 == ring.length ? 0 : slot + 1;
    Op op = ring[slot];
    switch (op.kind()) {
      case REST, CROSS -> {
        Order order = limit(op.side(), op.priceCents(), op.quantity());
        submittedIds[slot] = order.getId();
        bh.consume(book.submit(order));
      }
      case CANCEL -> bh.consume(book.cancel(submittedIds[op.slot()]));
    }
  }

  private Order limit(Side side, long priceCents, long quantity) {
    return new Order(++nextId, OWNER, side, OrderType.LIMIT, priceCents, quantity);
  }

  /**
   * Builds the ring so that one full pass leaves the book exactly as it started. Each unit of ten
   * operations works one side of the book, alternating sides. Six rests join the touch and two
   * crosses of three orders each take that quantity back out, so the touch never grows. One rest
   * lands on a random deeper level and the cancel that closes the unit removes it.
   */
  private static Op[] mixedRing(int depth) {
    Random random = new Random(42);
    List<Op> ops = new ArrayList<>();
    for (int unit = 0; unit < MIXED_UNITS; unit++) {
      boolean onAsks = unit % 2 == 0;
      Side maker = onAsks ? Side.SELL : Side.BUY;
      Side taker = onAsks ? Side.BUY : Side.SELL;
      long touch = onAsks ? BEST_ASK : BEST_BID;
      long away = (1 + random.nextInt(depth - 1)) * TICK;
      long deep = onAsks ? BEST_ASK + away : BEST_BID - away;

      int deepSlot = ops.size();
      ops.add(Op.submit(Kind.REST, maker, deep, QUANTITY));
      for (int sweep = 0; sweep < 2; sweep++) {
        for (int i = 0; i < 3; i++) {
          ops.add(Op.submit(Kind.REST, maker, touch, QUANTITY));
        }
        ops.add(Op.submit(Kind.CROSS, taker, touch, 3 * QUANTITY));
      }
      ops.add(Op.cancel(deepSlot));
    }
    return ops.toArray(Op[]::new);
  }

  private enum Kind {
    REST,
    CROSS,
    CANCEL
  }

  /** One ring step. A cancel names the ring slot whose submitted order it removes. */
  private record Op(Kind kind, Side side, long priceCents, long quantity, int slot) {
    static Op submit(Kind kind, Side side, long priceCents, long quantity) {
      return new Op(kind, side, priceCents, quantity, -1);
    }

    static Op cancel(int slot) {
      return new Op(Kind.CANCEL, null, 0, 0, slot);
    }
  }
}
