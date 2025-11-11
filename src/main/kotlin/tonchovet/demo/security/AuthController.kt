package tonchovet.demo.security

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import tonchovet.demo.repository.UserRepository

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): Mono<ResponseEntity<AuthResponse>> {
        return userRepository.findByUsername(req.username)
            .flatMap { user ->
                if (user.password == req.password) {
                    val token = jwtTokenProvider.generateToken(user.username)
                    Mono.just(ResponseEntity.ok(AuthResponse(token)))
                } else {
                    Mono.error(RuntimeException("Invalid credentials"))
                }
            }
            .switchIfEmpty(Mono.error(RuntimeException("User not found")))
    }
}

data class LoginRequest(val username: String, val password: String)
data class AuthResponse(val token: String)
