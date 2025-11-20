package com.example.demo.repository;


import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.User;


@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    User findByUsername(@Param("username") String username);
}
