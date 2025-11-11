package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    Flux<User> findByName(@Param("name") String name);
}
