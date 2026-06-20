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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

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
                .header(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(authResponse.getRefreshToken()).toString())
                .body(authResponse);
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(refreshTokenCookieSecure)
                .sameSite(refreshTokenCookieSameSite)
                .path(servletContextPath + "/auth")
                .maxAge(jwtService.getRefreshExpirationMs() / 1000)
                .build();
    }
}
