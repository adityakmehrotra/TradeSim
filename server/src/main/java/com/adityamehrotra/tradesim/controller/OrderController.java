package com.adityamehrotra.tradesim.controller;

import com.adityamehrotra.tradesim.dto.OrderRequest;
import com.adityamehrotra.tradesim.engine.OrderType;
import com.adityamehrotra.tradesim.engine.Side;
import com.adityamehrotra.tradesim.exception.RateLimitedException;
import com.adityamehrotra.tradesim.market.MarketService;
import com.adityamehrotra.tradesim.model.Lot;
import com.adityamehrotra.tradesim.model.Position;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.service.PortfolioService;
import com.adityamehrotra.tradesim.service.PositionService;
import com.adityamehrotra.tradesim.startup.StartupReconciliation;
import com.adityamehrotra.tradesim.web.RateLimiter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Places and cancels orders for the caller's session, and reports open orders and positions. */
@RestController
@RequestMapping("/tradesim/api/order")
public class OrderController {
  private final MarketService marketService;
  private final PositionService positionService;
  private final PortfolioService portfolioService;
  private final StartupReconciliation reconciliation;
  private final RateLimiter rateLimiter;
  private final int ordersPerMinute;

  public OrderController(
      MarketService marketService,
      PositionService positionService,
      PortfolioService portfolioService,
      StartupReconciliation reconciliation,
      RateLimiter rateLimiter,
      @Value("${tradesim.limits.orders-per-minute:120}") int ordersPerMinute) {
    this.marketService = marketService;
    this.positionService = positionService;
    this.portfolioService = portfolioService;
    this.reconciliation = reconciliation;
    this.rateLimiter = rateLimiter;
    this.ordersPerMinute = ordersPerMinute;
  }

  /**
   * Placing is the one path that has to wait for the startup sweep. Until it runs, reserved cash
   * from the previous run is still counted against the account, so an order would be refused or
   * sized against a balance that is not real. Nothing can be resting to cancel before then.
   */
  @PostMapping
  public ResponseEntity<?> place(Session session, @RequestBody OrderRequest request) {
    reconciliation.requireComplete();
    if (!rateLimiter.allow("order:" + session.getAccountID(), ordersPerMinute, 60_000L)) {
      throw new RateLimitedException("Too many orders. Slow down and try again shortly.");
    }
    try {
      portfolioService.requireOwned(request.getPortfolioID(), session.getAccountID());
      Side side = Side.valueOf(request.getSide().toUpperCase());
      OrderType type = OrderType.valueOf(request.getType().toUpperCase());
      MarketService.PlaceResult result =
          marketService.placeOrder(
              request.getPortfolioID(),
              session.getAccountID(),
              request.getSymbol(),
              side,
              type,
              request.getLimitPriceCents(),
              request.getQuantity());
      return ResponseEntity.ok(result);
    } catch (IllegalArgumentException | NullPointerException e) {
      return ResponseEntity.badRequest().body(Map.of("error", message(e)));
    }
  }

  @DeleteMapping
  public ResponseEntity<?> cancel(Session session, @RequestParam long orderId) {
    boolean cancelled = marketService.cancelOrder(session.getAccountID(), orderId);
    return ResponseEntity.ok(Map.of("cancelled", cancelled));
  }

  @GetMapping("/open")
  public ResponseEntity<?> openOrders(Session session) {
    return ResponseEntity.ok(marketService.openOrders(session.getAccountID()));
  }

  @GetMapping("/positions")
  public ResponseEntity<?> positions(Session session, @RequestParam Integer portfolioID) {
    portfolioService.requireOwned(portfolioID, session.getAccountID());
    List<Map<String, Object>> views = new ArrayList<>();
    for (Position position : positionService.positionsFor(portfolioID)) {
      if (position.getQuantity() <= 0) {
        continue;
      }
      long last = marketService.lastPrice(position.getSymbol());
      long costCents = 0;
      for (Lot lot : position.getLots()) {
        costCents += lot.getPriceCents() * lot.getQuantity();
      }
      long marketValueCents = Math.multiplyExact(last, position.getQuantity());

      Map<String, Object> view = new java.util.HashMap<>();
      view.put("symbol", position.getSymbol());
      view.put("quantity", position.getQuantity());
      view.put("available", position.availableQuantity());
      view.put("lastCents", last);
      view.put("marketValueCents", marketValueCents);
      view.put("costBasisCents", costCents);
      view.put("unrealizedPnlCents", marketValueCents - costCents);
      view.put("realizedPnlCents", position.getRealizedPnlCents());
      views.add(view);
    }
    return ResponseEntity.ok(views);
  }

  private static String message(Exception e) {
    return e.getMessage() == null ? "Invalid order request" : e.getMessage();
  }
}
