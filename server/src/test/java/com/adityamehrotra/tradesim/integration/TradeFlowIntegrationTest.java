package com.adityamehrotra.tradesim.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adityamehrotra.tradesim.market.Instruments;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

/** TestSession to fill to settled position, over real HTTP against a real database. */
class TradeFlowIntegrationTest extends IntegrationTestBase {
  private static final String SYMBOL = Instruments.ALL.get(0).symbol();
  private static final double STARTER_CASH = 100000.0;

  @Test
  void seedsAStarterPortfolioForANewSession() {
    TestSession session = openSession();

    assertTrue(session.accountId() > 0);
    Map<String, Object> portfolio =
        getMap(session, "/tradesim/api/portfolio/get?portfolioID=" + session.firstPortfolioId());
    assertEquals(STARTER_CASH, (double) portfolio.get("cash"));
    assertEquals(STARTER_CASH, (double) portfolio.get("initialBalance"));
  }

  @Test
  void marketBuyFillsAgainstTheBotQuotesAndSettles() {
    TestSession session = openSession();
    int portfolioId = session.firstPortfolioId();

    Map<String, Object> result = placeOrder(session, marketBuy(portfolioId, SYMBOL, 10));

    assertEquals(10, ((Number) result.get("filled")).intValue());
    assertEquals(0, ((Number) result.get("remaining")).intValue());
    assertEquals(false, result.get("resting"));

    List<Map<String, Object>> positions =
        getList(session, "/tradesim/api/order/positions?portfolioID=" + portfolioId);
    assertEquals(1, positions.size());
    assertEquals(SYMBOL, positions.get(0).get("symbol"));
    assertEquals(10, ((Number) positions.get(0).get("quantity")).intValue());

    Map<String, Object> portfolio =
        getMap(session, "/tradesim/api/portfolio/get?portfolioID=" + portfolioId);
    double cash = (double) portfolio.get("cash");
    assertTrue(cash < STARTER_CASH, "buying should spend cash, cash was " + cash);
  }

  @Test
  void limitSellRestsReportsItsPriceAndCancelsBack() {
    TestSession session = openSession();
    int portfolioId = session.firstPortfolioId();
    placeOrder(session, marketBuy(portfolioId, SYMBOL, 10));

    long farAboveMarket = 9_000_000;
    Map<String, Object> result =
        placeOrder(session, limitOrder(portfolioId, SYMBOL, "SELL", farAboveMarket, 10));
    assertEquals(true, result.get("resting"));
    assertEquals(0, ((Number) result.get("filled")).intValue());

    List<Map<String, Object>> open = getList(session, "/tradesim/api/order/open");
    assertEquals(1, open.size());
    // A sell reserves shares rather than cash, so this is the field that used to report zero.
    assertEquals(farAboveMarket, ((Number) open.get(0).get("limitPriceCents")).longValue());

    long orderId = ((Number) open.get(0).get("orderId")).longValue();
    Map<String, Object> cancelled =
        exchangeMap(session, HttpMethod.DELETE, "/tradesim/api/order?orderId=" + orderId);
    assertEquals(true, cancelled.get("cancelled"));
    assertTrue(getList(session, "/tradesim/api/order/open").isEmpty());
  }

  @Test
  void sellingBackRealisesProfitAgainstTheFifoCostBasis() {
    TestSession session = openSession();
    int portfolioId = session.firstPortfolioId();

    placeOrder(session, marketBuy(portfolioId, SYMBOL, 10));
    placeOrder(session, marketBuy(portfolioId, SYMBOL, 10));
    Map<String, Object> sold = placeOrder(session, limitOrder(portfolioId, SYMBOL, "SELL", 1, 15));
    assertEquals(15, ((Number) sold.get("filled")).intValue());

    List<Map<String, Object>> positions =
        getList(session, "/tradesim/api/order/positions?portfolioID=" + portfolioId);
    assertEquals(5, ((Number) positions.get(0).get("quantity")).intValue());
    assertNotNull(positions.get(0).get("realizedPnl"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void resetClearsPositionsAndRestingOrdersAndSeedsAgain() {
    TestSession session = openSession();
    int portfolioId = session.firstPortfolioId();
    placeOrder(session, marketBuy(portfolioId, SYMBOL, 10));
    placeOrder(session, limitOrder(portfolioId, SYMBOL, "SELL", 9_000_000, 10));

    send(session, HttpMethod.POST, "/tradesim/api/session/reset", null);

    assertTrue(
        getList(session, "/tradesim/api/order/open").isEmpty(), "reset should cancel orders");
    Map<String, Object> body = getMap(session, "/tradesim/api/session");
    List<Map<String, Object>> portfolios = (List<Map<String, Object>>) body.get("portfolios");
    assertEquals(1, portfolios.size());
    assertEquals(STARTER_CASH, (double) portfolios.get(0).get("cash"));

    int freshId = (int) portfolios.get(0).get("portfolioID");
    assertTrue(
        getList(session, "/tradesim/api/order/positions?portfolioID=" + freshId).isEmpty(),
        "reset should leave no positions behind");
  }
}
