package com.adityamehrotra.tradesim.integration;

import com.adityamehrotra.tradesim.service.SessionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.MongoDBContainer;

/**
 * Drives the real HTTP surface against a real MongoDB. The market clock is switched off so the book
 * only moves when a test moves it, which is what lets these assertions be exact rather than
 * tolerant of a price that keeps drifting.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "tradesim.market.autotick=false")
abstract class IntegrationTestBase {
  /**
   * One container for every integration class. Tying its lifecycle to a single class would stop it
   * when that class finished, leaving the cached Spring context pointing at a port nothing answers
   * on. Testcontainers reaps it when the JVM exits.
   */
  static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

  static {
    MONGO.start();
  }

  @DynamicPropertySource
  static void mongoProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
  }

  @Autowired protected TestRestTemplate rest;
  @Autowired private MongoTemplate mongoTemplate;

  private final List<Session> opened = new ArrayList<>();

  /**
   * The order books live in memory and outlive any one test, so resting orders are cleared through
   * the API before the documents go. Dropping the collections alone would leave orders in the book
   * pointing at portfolios that no longer exist.
   */
  @AfterEach
  void clearState() {
    for (Session session : opened) {
      rest.exchange(
          "/tradesim/api/session/reset",
          HttpMethod.POST,
          new HttpEntity<>(headers(session)),
          String.class);
    }
    opened.clear();
    for (String name : mongoTemplate.getDb().listCollectionNames()) {
      mongoTemplate.getDb().getCollection(name).deleteMany(new Document());
    }
  }

  /** Opens a session and remembers it, so its orders are cleared when the test finishes. */
  protected Session openSession() {
    ResponseEntity<Map<String, Object>> response =
        rest.exchange(
            "/tradesim/api/session",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {});
    String setCookie = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    Session session = new Session(setCookie.split(";")[0], response.getBody());
    opened.add(session);
    return session;
  }

  protected HttpHeaders headers(Session session) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(HttpHeaders.COOKIE, session.cookie());
    return headers;
  }

  protected ResponseEntity<String> send(
      Session session, HttpMethod method, String path, String body) {
    return rest.exchange(path, method, new HttpEntity<>(body, headers(session)), String.class);
  }

  protected ResponseEntity<String> get(Session session, String path) {
    return rest.exchange(path, HttpMethod.GET, new HttpEntity<>(headers(session)), String.class);
  }

  protected Map<String, Object> getMap(Session session, String path) {
    return rest.exchange(
            path,
            HttpMethod.GET,
            new HttpEntity<>(headers(session)),
            new ParameterizedTypeReference<Map<String, Object>>() {})
        .getBody();
  }

  protected List<Map<String, Object>> getList(Session session, String path) {
    return rest.exchange(
            path,
            HttpMethod.GET,
            new HttpEntity<>(headers(session)),
            new ParameterizedTypeReference<List<Map<String, Object>>>() {})
        .getBody();
  }

  protected Map<String, Object> exchangeMap(Session session, HttpMethod method, String path) {
    return rest.exchange(
            path,
            method,
            new HttpEntity<>(headers(session)),
            new ParameterizedTypeReference<Map<String, Object>>() {})
        .getBody();
  }

  protected Map<String, Object> placeOrder(Session session, String body) {
    return rest.exchange(
            "/tradesim/api/order",
            HttpMethod.POST,
            new HttpEntity<>(body, headers(session)),
            new ParameterizedTypeReference<Map<String, Object>>() {})
        .getBody();
  }

  protected static String marketBuy(int portfolioId, String symbol, int quantity) {
    return """
        {"portfolioID":%d,"symbol":"%s","side":"BUY","type":"MARKET","quantity":%d}"""
        .formatted(portfolioId, symbol, quantity);
  }

  protected static String limitOrder(
      int portfolioId, String symbol, String side, long priceCents, int quantity) {
    return """
        {"portfolioID":%d,"symbol":"%s","side":"%s","type":"LIMIT","limitPriceCents":%d,\
        "quantity":%d}"""
        .formatted(portfolioId, symbol, side, priceCents, quantity);
  }

  protected static String cookieName() {
    return SessionService.COOKIE_NAME;
  }

  /** A session cookie plus the body the session endpoint returned when it was created. */
  protected record Session(String cookie, Map<String, Object> body) {
    @SuppressWarnings("unchecked")
    int firstPortfolioId() {
      return (int) ((List<Map<String, Object>>) body.get("portfolios")).get(0).get("portfolioID");
    }

    int accountId() {
      return (int) body.get("accountId");
    }
  }
}
