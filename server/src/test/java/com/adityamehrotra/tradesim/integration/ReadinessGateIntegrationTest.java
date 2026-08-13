package com.adityamehrotra.tradesim.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.adityamehrotra.tradesim.market.Instruments;
import com.adityamehrotra.tradesim.startup.ReservationSweep;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

/**
 * The database is there for everything except the sweep, which is the state the readiness gate
 * exists for. The retry is pushed out past the end of the test so the gate stays shut.
 */
@TestPropertySource(properties = "tradesim.startup.first-retry-ms=60000")
class ReadinessGateIntegrationTest extends IntegrationTestBase {
  private static final String SYMBOL = Instruments.ALL.get(0).symbol();

  @TestConfiguration
  static class SweepThatCannotReachTheDatabase {
    @Bean
    @Primary
    ReservationSweep failingSweep() {
      ReservationSweep sweep = mock(ReservationSweep.class);
      doThrow(new DataAccessResourceFailureException("mongo is away")).when(sweep).run();
      return sweep;
    }
  }

  @Test
  void readinessStaysDownWhileTheSweepKeepsFailing() {
    var response = rest.getForEntity("/actuator/health/readiness", String.class);

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    assertEquals("{\"status\":\"DOWN\"}", response.getBody());
  }

  @Test
  void livenessIsStillUpSoNothingRestartsTheProcess() {
    var response = rest.getForEntity("/actuator/health/liveness", String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void ordersAreRefusedWhileTheSweepKeepsFailing() {
    TestSession session = openSession();

    var response =
        send(
            session,
            HttpMethod.POST,
            "/tradesim/api/order",
            marketBuy(session.firstPortfolioId(), SYMBOL, 10));

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
  }
}
