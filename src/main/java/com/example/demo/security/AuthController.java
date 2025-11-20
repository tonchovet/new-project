package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthController(UserRepository userRepository,
                          JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername());
        if (user != null && user.getPassword().equals(req.getPassword())) {
            String token = jwtTokenProvider.generateToken(user.getUsername());
            return ResponseEntity.ok(new AuthResponse(token));
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}
