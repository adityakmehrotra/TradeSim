package com.adityamehrotra.tradesim.controller;

import com.adityamehrotra.tradesim.dto.OrderRequest;
import com.adityamehrotra.tradesim.engine.OrderType;
import com.adityamehrotra.tradesim.engine.Side;
import com.adityamehrotra.tradesim.market.MarketService;
import com.adityamehrotra.tradesim.model.Lot;
import com.adityamehrotra.tradesim.model.Position;
import com.adityamehrotra.tradesim.model.Session;
import com.adityamehrotra.tradesim.service.PositionService;
import com.adityamehrotra.tradesim.service.SessionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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
  private static final String COOKIE_NAME = "tradesim_session";

  private final MarketService marketService;
  private final PositionService positionService;
  private final SessionService sessionService;

  public OrderController(
      MarketService marketService, PositionService positionService, SessionService sessionService) {
    this.marketService = marketService;
    this.positionService = positionService;
    this.sessionService = sessionService;
  }

  @PostMapping
  public ResponseEntity<?> place(
      @CookieValue(name = COOKIE_NAME, required = false) String token,
      @RequestBody OrderRequest request) {
    try {
      Session session = sessionService.getOrCreate(token);
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
  public ResponseEntity<?> cancel(
      @CookieValue(name = COOKIE_NAME, required = false) String token, @RequestParam long orderId) {
    Session session = sessionService.getOrCreate(token);
    boolean cancelled = marketService.cancelOrder(session.getAccountID(), orderId);
    return ResponseEntity.ok(Map.of("cancelled", cancelled));
  }

  @GetMapping("/open")
  public ResponseEntity<?> openOrders(
      @CookieValue(name = COOKIE_NAME, required = false) String token) {
    Session session = sessionService.getOrCreate(token);
    return ResponseEntity.ok(marketService.openOrders(session.getAccountID()));
  }

  @GetMapping("/positions")
  public ResponseEntity<?> positions(@RequestParam Integer portfolioID) {
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
      double marketValue = (last * position.getQuantity()) / 100.0;
      double costBasis = costCents / 100.0;

      Map<String, Object> view = new java.util.HashMap<>();
      view.put("symbol", position.getSymbol());
      view.put("quantity", position.getQuantity());
      view.put("lastCents", last);
      view.put("marketValue", marketValue);
      view.put("costBasis", costBasis);
      view.put("unrealizedPnl", marketValue - costBasis);
      view.put("realizedPnl", position.getRealizedPnl());
      views.add(view);
    }
    return ResponseEntity.ok(views);
  }

  private static String message(Exception e) {
    return e.getMessage() == null ? "Invalid order request" : e.getMessage();
  }
}
