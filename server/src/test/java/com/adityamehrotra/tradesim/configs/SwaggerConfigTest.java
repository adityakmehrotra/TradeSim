package com.adityamehrotra.tradesim.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

/** The documented version follows the build, and still resolves when the build info is missing. */
class SwaggerConfigTest {

  @Test
  void readsTheVersionFromTheBuildInfo() {
    Properties properties = new Properties();
    properties.setProperty("version", "1.2.3");

    assertEquals("1.2.3", SwaggerConfig.version(new BuildProperties(properties)));
  }

  @Test
  void fallsBackWhenTheBuildInfoIsMissing() {
    assertEquals(SwaggerConfig.UNBUILT_VERSION, SwaggerConfig.version(null));
  }
}
