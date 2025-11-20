package com.example.demo.controller;


import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public Flux<User> list() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return userRepository.findAll(sort);
    }
}
