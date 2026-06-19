package dnsolutions.fi.SelfDevelopment.controller;

import dnsolutions.fi.SelfDevelopment.dto.AddUserRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.AuthLoginRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.AuthResponseDTO;
import dnsolutions.fi.SelfDevelopment.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody AddUserRequestDTO addUserRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(addUserRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthLoginRequestDTO authLoginRequestDTO) {
        return ResponseEntity.ok(authService.login(authLoginRequestDTO));
    }
}
