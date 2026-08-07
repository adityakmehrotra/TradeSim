package com.adityamehrotra.tradesim.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  static final String UNBUILT_VERSION = "development";

  @Bean
  public OpenAPI openApi(ObjectProvider<BuildProperties> buildProperties) {
    return new OpenAPI()
        .info(
            new Info()
                .title("TradeSim API")
                .version(version(buildProperties.getIfAvailable()))
                .description(
                    """
                    ### API documentation for the TradeSim backend. Read more in the GitHub repository:

                    #### https://github.com/adityakmehrotra/TradeSim
                    """)
                .license(new License().name("MIT License"))
                .contact(new Contact().name("Aditya Mehrotra")));
  }

  /**
   * Maven generates the build info, so the documented version tracks the project version. Starting
   * the application straight from an IDE skips that step, which is why this falls back instead of
   * failing.
   */
  static String version(BuildProperties buildProperties) {
    return buildProperties == null ? UNBUILT_VERSION : buildProperties.getVersion();
  }
}
