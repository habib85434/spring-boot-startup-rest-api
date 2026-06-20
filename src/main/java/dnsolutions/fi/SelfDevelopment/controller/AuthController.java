package dnsolutions.fi.SelfDevelopment.controller;

import dnsolutions.fi.SelfDevelopment.dto.AddUserRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.AuthLoginRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.AuthResponseDTO;
import dnsolutions.fi.SelfDevelopment.security.JwtService;
import dnsolutions.fi.SelfDevelopment.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    private final AuthService authService;
    private final JwtService jwtService;

    @Value("${server.servlet.context-path:}")
    private String servletContextPath;

    @Value("${application.security.refresh-token.cookie-secure:true}")
    private boolean refreshTokenCookieSecure;

    @Value("${application.security.refresh-token.cookie-same-site:Strict}")
    private String refreshTokenCookieSameSite;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody AddUserRequestDTO addUserRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(addUserRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthLoginRequestDTO authLoginRequestDTO) {
        AuthResponseDTO authResponse = authService.login(authLoginRequestDTO);
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        buildRefreshTokenCookie(
                                authResponse.getRefreshToken(),
                                jwtService.getRefreshExpirationMs() / 1000
                        ).toString()
                )
                .body(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        authService.logout(extractBearerToken(authorizationHeader));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie("", 0).toString())
                .build();
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(refreshTokenCookieSecure)
                .sameSite(refreshTokenCookieSameSite)
                .path(servletContextPath + "/auth")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}
