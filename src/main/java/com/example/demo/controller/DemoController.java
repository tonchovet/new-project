package com.example.demo.controller;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
public class DemoController {

    private final UserRepository userRepo;

    public DemoController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping
    public Flux<User> all() {
        return userRepo.findAll();
    }

    @PostMapping
    public Mono<User> create(@RequestBody User user) {
        return userRepo.save(user);
    }
}
