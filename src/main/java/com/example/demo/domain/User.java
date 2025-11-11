package com.example.demo.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public class User {
    @Id
    private Long id;
    private String name;
    private BigInteger balanceWei; // optional Ethereum balance

    // getters / setters
}
