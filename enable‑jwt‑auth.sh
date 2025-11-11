#!/usr/bin/env bash
# --------------------------------------------
# enable-jwt-auth.sh  –  patched for sed compatibility
# --------------------------------------------
set -euo pipefail

# 0️⃣  Repository root
PROJECT_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$PROJECT_ROOT"

# --------------------------------------------
# 1️⃣  Dependency block – write to temp file
DEP_SNIPPET=$(cat <<'EOF'
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
EOF
)

tmpdep=$(mktemp)
printf '%s\n' "$DEP_SNIPPET" > "$tmpdep"

# Append the snippet **after** <dependencies>
sed -i.bak "/<dependencies>/r $tmpdep" pom.xml
rm "$tmpdep"
echo "✅ Added Spring‑Security + JWT dependencies to pom.xml."

# --------------------------------------------
# 2️⃣  Add JWT properties to application.yml
YML_SNIPPET=$(cat <<'EOF'
jwt:
  secret: "CHANGE_ME_TO_A_SECURE_RANDOM_SECRET"
  expirationMs: 86400000   # 1 day
EOF
)

tmpyml=$(mktemp)
printf '%s\n' "$YML_SNIPPET" > "$tmpyml"

# If the file already contains `jwt:`, skip it
if grep -q "^jwt:" src/main/resources/application.yml; then
    echo "⚠️  JWT properties already present – leaving unchanged."
else
    # Insert just before the last line that starts with `---` (or at EOF)
    if grep -q "^---" src/main/resources/application.yml; then
        sed -i.bak "/^---/r $tmpyml" src/main/resources/application.yml
    else
        cat "$tmpyml" >> src/main/resources/application.yml
    fi
    rm "$tmpyml"
    echo "✅ Added JWT configuration to application.yml."
fi

# --------------------------------------------
# 3️⃣  Create JwtTokenProvider.kt (kept the same as before)
cat > src/main/kotlin/tonchovet/demo/security/JwtTokenProvider.kt <<'KOT'
package tonchovet.demo.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val jwtSecret: String,
    @Value("\${jwt.expirationMs}") private val jwtExpirationMs: Long
) {
    private val key: Key = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

    fun generateToken(username: String): String {
        val now = Date()
        val expiry = Date(now.time + jwtExpirationMs)

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getUsernameFromJwt(token: String): String? =
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body.subject
        } catch (e: Exception) { null }

    fun validateToken(token: String): Boolean =
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body.expiration.after(Date())
        } catch (e: Exception) { false }
}
KOT
echo "✅ Created JwtTokenProvider.kt"

# --------------------------------------------
# 4️⃣  AuthController.kt  (kept the same as before)
cat > src/main/kotlin/tonchovet/demo/security/AuthController.kt <<'KOT'
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
KOT
echo "✅ Created AuthController.kt"

# --------------------------------------------
# 5️⃣  SecurityConfig.kt  (kept the same as before)
cat > src/main/kotlin/tonchovet/demo/security/SecurityConfig.kt <<'KOT'
package tonchovet.demo.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono
import tonchovet.demo.repository.UserRepository
import org.springframework.security.web.server.WebFilter
import org.springframework.core.annotation.Order

@Configuration
class SecurityConfig(
    private val jwtProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        val jwtAuthFilter = WebFilter { exchange, chain ->
            val header = exchange.request.headers.getFirst("Authorization")
            if (header.isNullOrBlank() || !header.startsWith("Bearer ")) {
                chain.filter(exchange)
            } else {
                val token = header.removePrefix("Bearer ")
                val username = jwtProvider.getUsernameFromJwt(token)
                if (username != null) {
                    userRepository.findByUsername(username)
                        .map { user ->
                            UsernamePasswordAuthenticationToken(user.username, null, listOf())
                        }
                        .flatMap { auth ->
                            chain.filter(exchange)
                        }
                        .switchIfEmpty(Mono.error(RuntimeException("User not found")))
                } else {
                    chain.filter(exchange)
                }
            }
        }

        return http
            .csrf().disable()
            .authorizeExchange()
                .pathMatchers("/api/auth/**").permitAll()
                .anyExchange().authenticated()
                .and()
            .addFilterAt(jwtAuthFilter, Order.FIRST)
            .build()
    }
}
KOT
echo "✅ Created SecurityConfig.kt"

echo "🎉 All files have been patched – next step: `./mvnw clean package` (or your usual build command)."
