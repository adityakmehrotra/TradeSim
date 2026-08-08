package com.adityamehrotra.tradesim.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adityamehrotra.tradesim.market.Instruments;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Two real sessions, checked against each other over HTTP. */
class PortfolioOwnershipIntegrationTest extends IntegrationTestBase {
  private static final String SYMBOL = Instruments.ALL.get(0).symbol();

  @Test
  void oneSessionCannotReachAnotherSessionsPortfolio() {
    Session owner = openSession();
    Session other = openSession();
    int target = owner.firstPortfolioId();
    assertNotEquals(target, other.firstPortfolioId());

    assertEquals(HttpStatus.NOT_FOUND, get(other, path(target)).getStatusCode());
    assertEquals(
        HttpStatus.NOT_FOUND,
        send(other, HttpMethod.DELETE, "/tradesim/api/portfolio/delete?portfolioID=" + target, null)
            .getStatusCode());
    assertEquals(
        HttpStatus.NOT_FOUND,
        send(
                other,
                HttpMethod.PUT,
                "/tradesim/api/portfolio/name?portfolioID=" + target + "&name=Theirs",
                null)
            .getStatusCode());

    // The owner is untouched by any of that.
    assertEquals(HttpStatus.OK, get(owner, path(target)).getStatusCode());
  }

  @Test
  void aForeignPortfolioIsIndistinguishableFromOneThatNeverExisted() {
    Session owner = openSession();
    Session other = openSession();

    ResponseEntity<String> foreign = get(other, path(owner.firstPortfolioId()));
    ResponseEntity<String> missing = get(other, path(999_999));

    assertEquals(foreign.getStatusCode(), missing.getStatusCode());
    assertEquals(foreign.getBody(), missing.getBody());
  }

  @Test
  void anOrderCannotBePlacedAgainstAnotherSessionsPortfolio() {
    Session owner = openSession();
    Session other = openSession();

    ResponseEntity<String> response =
        send(
            other,
            HttpMethod.POST,
            "/tradesim/api/order",
            marketBuy(owner.firstPortfolioId(), SYMBOL, 5));

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertTrue(
        getList(owner, "/tradesim/api/order/positions?portfolioID=" + owner.firstPortfolioId())
            .isEmpty(),
        "the owner should have no position from someone else's order");
  }

  @Test
  void creatingAPortfolioIgnoresTheAccountInTheBody() {
    Session owner = openSession();
    Session other = openSession();

    send(
        other,
        HttpMethod.POST,
        "/tradesim/api/portfolio/create",
        """
        {"accountID":%d,"name":"Claimed","description":"claims another account","cash":5000.0,\
        "initialBalance":5000.0}"""
            .formatted(owner.accountId()));

    List<Map<String, Object>> ownerPortfolios = portfoliosOf(owner);
    assertEquals(1, ownerPortfolios.size(), "the claimed account should gain nothing");
    assertEquals(2, portfoliosOf(other).size(), "the caller's own account gains it instead");
  }

  @Test
  void deletingAPortfolioClearsItsRestingOrdersAndPositions() {
    Session session = openSession();
    int portfolioId = session.firstPortfolioId();
    placeOrder(session, marketBuy(portfolioId, SYMBOL, 10));
    placeOrder(session, limitOrder(portfolioId, SYMBOL, "SELL", 9_000_000, 10));
    assertEquals(1, getList(session, "/tradesim/api/order/open").size());

    ResponseEntity<String> deleted =
        send(
            session,
            HttpMethod.DELETE,
            "/tradesim/api/portfolio/delete?portfolioID=" + portfolioId,
            null);

    assertEquals(HttpStatus.OK, deleted.getStatusCode());
    assertTrue(
        getList(session, "/tradesim/api/order/open").isEmpty(),
        "deleting a portfolio should cancel the orders resting against it");
    assertEquals(HttpStatus.NOT_FOUND, get(session, path(portfolioId)).getStatusCode());
  }

  @Test
  void requestsWithoutASessionAreRefusedAndCreateNothing() {
    ResponseEntity<String> response =
        rest.getForEntity("/tradesim/api/portfolio/get?portfolioID=1", String.class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> portfoliosOf(Session session) {
    return (List<Map<String, Object>>) getMap(session, "/tradesim/api/session").get("portfolios");
  }

  private static String path(int portfolioId) {
    return "/tradesim/api/portfolio/get?portfolioID=" + portfolioId;
  }
}
