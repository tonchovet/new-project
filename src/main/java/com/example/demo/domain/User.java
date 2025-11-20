package com.example.demo.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private BigInteger balanceWei;
    private String username;
    private String password;
}
