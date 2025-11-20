package com.example.demo.config;

import com.example.demo.R2dbcEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.r2dbc.RepositoryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.ConnectionFactory;
import org.springframework.r2dbc.core.ConnectionFactoryUtils;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
@EnableR2dbcRepositories
public class R2dbcConfig {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Bean
    public R2dbcEntityTemplate r2dbcTemplate() {
        DatabaseClient client = DatabaseClient.builder()
                .connectionFactory(connectionFactory)
                .build();
        return new R2dbcEntityTemplate(client);
    }

    @Bean
    public ConnectionFactory r2dbcConnectionFactory() {
        return ConnectionFactoryUtils.getConnectionFactory(connectionFactory);
    }
}
