package com.jcaa.usersmanagement.infrastructure.security;

import com.jcaa.usersmanagement.domain.enums.UserRole;
import com.jcaa.usersmanagement.domain.model.UserModel;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {

  private static final String ROLE_CLAIM = "role";

  private final SecretKey signingKey;
  private final long expirationSeconds;
  private final Clock clock;

  @Autowired
  public JwtTokenService(final JwtProperties properties) {
    this(properties, Clock.systemUTC());
  }

  JwtTokenService(final JwtProperties properties, final Clock clock) {
    this.signingKey = Keys.hmacShaKeyFor(resolveKeyBytes(properties.secret()));
    this.expirationSeconds = properties.expirationSeconds();
    this.clock = clock;
  }

  private static byte[] resolveKeyBytes(final String secret) {
    if (secret == null || secret.isBlank()) {
      return "development-only-jwt-secret-must-be-replaced-32b".getBytes(StandardCharsets.UTF_8);
    }
    try {
      final byte[] decoded = Base64.getDecoder().decode(secret);
      if (decoded.length >= 32) {
        return decoded;
      }
    } catch (final IllegalArgumentException ignored) {
      // not base64 encoded
    }
    final byte[] rawBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (rawBytes.length >= 32) {
      return rawBytes;
    }
    try {
      return MessageDigest.getInstance("SHA-256").digest(rawBytes);
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 no disponible", e);
    }
  }

  public String generate(final UserModel user) {
    final Instant issuedAt = clock.instant();
    final Instant expiration = issuedAt.plusSeconds(expirationSeconds);
    return Jwts.builder()
        .subject(user.getId().value())
        .claim(ROLE_CLAIM, user.getRole().name())
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(expiration))
        .signWith(signingKey)
        .compact();
  }

  public JwtPrincipal parse(final String token) {
    final Claims claims =
        Jwts.parser()
            .verifyWith(signingKey)
            .clock(() -> Date.from(clock.instant()))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    return new JwtPrincipal(claims.getSubject(), UserRole.fromString(claims.get(ROLE_CLAIM, String.class)));
  }

  public long expirationSeconds() {
    return expirationSeconds;
  }
}
