package com.example.demo.config;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Web3jConfig {

    @Bean
    public Web3j web3j() {
        String rpcUrl = System.getenv().getOrDefault("WEB3J_RPC_URL", "http://localhost:8545");
        return Web3j.build(new HttpService(rpcUrl));
    }
}
