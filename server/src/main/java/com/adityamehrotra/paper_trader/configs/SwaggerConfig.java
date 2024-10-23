package com.adityamehrotra.paper_trader.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Paper Trader Backend - Used by Paper Trader UI")
                .version("1.4.7")
                .description("""
                    ### API Documentation for TradeSim Backend. Read more about TradeSim in the GitHub Repository:
                    
                    #### https://github.com/adityakmehrotra/tradesim
                    """)
                .license(new License().name("Copyright 2024 Aditya Mehrotra"))
                .contact(new Contact().name("Aditya Mehrotra")));
  }
}
