package com.adityamehrotra.tradesim.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Health is the only thing actuator is allowed to answer, and both probes have to answer it. */
class ActuatorIntegrationTest extends IntegrationTestBase {
  private static final String UP = "{\"status\":\"UP\"}";

  @Test
  void answersHealthAndBothProbes() {
    assertEquals(
        HttpStatus.OK, rest.getForEntity("/actuator/health", String.class).getStatusCode());
    assertEquals(UP, rest.getForEntity("/actuator/health/liveness", String.class).getBody());
    assertEquals(UP, rest.getForEntity("/actuator/health/readiness", String.class).getBody());
  }

  @Test
  void leavesEveryOtherEndpointUnexposed() {
    List<String> hidden =
        List.of("/actuator/env", "/actuator/beans", "/actuator/metrics", "/actuator/loggers");

    for (String path : hidden) {
      assertEquals(
          HttpStatus.NOT_FOUND, rest.getForEntity(path, String.class).getStatusCode(), path);
    }
  }
}
