package com.example.demo.config;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions.*;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.example.demo.repository")
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    @Bean
    @Override
    public ConnectionFactory connectionFactory() {
        String url = System.getenv().getOrDefault("R2DBC_URL", "r2dbc:mysql://localhost:3306/demo");
        String user = System.getenv().getOrDefault("R2DBC_USER", "root");
        String pass = System.getenv().getOrDefault("R2DBC_PASSWORD", "root");

        ConnectionFactoryOptions options = ConnectionFactoryOptions.parse(url).mutate()
                .option(USER, user)
                .option(PASSWORD, pass)
                .build();

        return io.r2dbc.spi.ConnectionFactories.get(options);
    }
}
