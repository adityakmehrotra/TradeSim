package com.adityamehrotra.tradesim.configs;

import com.adityamehrotra.tradesim.web.SessionArgumentResolver;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  /**
   * One or more exact origins, separated by commas. Requests carry the session cookie, so this
   * stays a list of origins that were named on purpose. No wildcard: a browser refuses one anyway
   * once credentials are involved, and silently allowing every site to call the api with a
   * visitor's cookie is the thing being avoided.
   */
  @Value("${tradesim.client-origin:http://localhost:5173}")
  private String clientOrigins;

  private final SessionArgumentResolver sessionArgumentResolver;

  public WebConfig(SessionArgumentResolver sessionArgumentResolver) {
    this.sessionArgumentResolver = sessionArgumentResolver;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins(origins())
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowCredentials(true);
  }

  private String[] origins() {
    return Arrays.stream(clientOrigins.split(","))
        .map(String::trim)
        .filter(origin -> !origin.isEmpty())
        .toArray(String[]::new);
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(sessionArgumentResolver);
  }
}
