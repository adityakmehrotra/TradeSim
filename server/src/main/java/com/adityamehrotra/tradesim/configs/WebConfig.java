package com.adityamehrotra.tradesim.configs;

import com.adityamehrotra.tradesim.web.SessionArgumentResolver;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${tradesim.client-origin:http://localhost:5173}")
  private String clientOrigin;

  private final SessionArgumentResolver sessionArgumentResolver;

  public WebConfig(SessionArgumentResolver sessionArgumentResolver) {
    this.sessionArgumentResolver = sessionArgumentResolver;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins(clientOrigin)
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowCredentials(true);
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(sessionArgumentResolver);
  }
}
