package com.example.demo;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class DemoApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepo;

    @Test
    public void testCreateAndRetrieveUser() {
        User user = new User();
        user.setName("Bob");
        user.setBalanceWei(BigInteger.valueOf(1000000000000L));

        // Create the user
        webTestClient.post()
                .uri("/api/users")
                .body(Mono.just(user), User.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .value(createdUser -> {
                    assertNotNull(createdUser.getId());
                    assertEquals("Bob", createdUser.getName());
                    assertEquals(BigInteger.valueOf(1000000000000L), createdUser.getBalanceWei());
                });

        // Retrieve the list of users
        webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .value(users -> {
                    assertFalse(users.isEmpty(), "User list should not be empty");
                    User retrieved = users.get(0);
                    assertEquals("Bob", retrieved.getName());
                    assertEquals(BigInteger.valueOf(1000000000000L), retrieved.getBalanceWei());
                });
    }

    @Test
    public void testFindByName() {
        User user = new User();
        user.setName("Charlie");

        webTestClient.post()
                .uri("/api/users")
                .bodyValue(user)
                .exchange()
                .expectStatus().isOk();

        StepVerifier.create(userRepo.findByName("Charlie"))
                .expectNextMatches(u -> "Charlie".equals(u.getName()))
                .expectComplete()
                .verify();
    }
}
