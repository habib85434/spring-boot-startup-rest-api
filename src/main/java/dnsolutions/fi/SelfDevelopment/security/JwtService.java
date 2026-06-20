package dnsolutions.fi.SelfDevelopment.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dnsolutions.fi.SelfDevelopment.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final byte[] secret;
    private final long expirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${application.security.jwt.secret}") String secret,
            @Value("${application.security.jwt.expiration-ms}") long expirationMs,
            @Value("${application.security.jwt.refresh-expiration-ms}") long refreshExpirationMs
    ) {
        this.secret = Base64.getDecoder().decode(secret);
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(userDetails, expirationMs, "access");
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshExpirationMs, "refresh");
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    private String generateToken(UserDetails userDetails, long tokenExpirationMs, String tokenUse) {
        Instant now = Instant.now();
        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub", userDetails.getUsername());
        claims.put("iat", now.getEpochSecond());
        claims.put("exp", now.plusMillis(tokenExpirationMs).getEpochSecond());
        claims.put("token_use", tokenUse);
        claims.put("roles", userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList());

        String unsignedToken = encodeJson(header) + "." + encodeJson(claims);
        return unsignedToken + "." + sign(unsignedToken);
    }

    public String extractUsername(String token) {
        return readClaims(token).get("sub").toString();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && hasValidSignature(token)
                && hasTokenUse(token, "access");
    }

    private boolean isTokenExpired(String token) {
        Object expiration = readClaims(token).get("exp");
        if (!(expiration instanceof Number expirationNumber)) {
            return true;
        }
        return Instant.now().getEpochSecond() >= expirationNumber.longValue();
    }

    private boolean hasValidSignature(String token) {
        String[] parts = splitToken(token);
        return sign(parts[0] + "." + parts[1]).equals(parts[2]);
    }

    private boolean hasTokenUse(String token, String tokenUse) {
        return tokenUse.equals(readClaims(token).get("token_use"));
    }

    private Map<String, Object> readClaims(String token) {
        String[] parts = splitToken(token);
        try {
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(payload, new TypeReference<>() {
            });
        } catch (IllegalArgumentException | IOException exception) {
            throw new BadRequestException("Invalid JWT token");
        }
    }

    private String[] splitToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new BadRequestException("Invalid JWT token");
        }
        return parts;
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not encode JWT", exception);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign JWT", exception);
        }
    }
}
