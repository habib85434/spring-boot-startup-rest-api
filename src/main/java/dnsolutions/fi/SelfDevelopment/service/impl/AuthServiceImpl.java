package dnsolutions.fi.SelfDevelopment.service.impl;

import dnsolutions.fi.SelfDevelopment.dto.AddUserRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.AuthLoginRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.AuthResponseDTO;
import dnsolutions.fi.SelfDevelopment.dto.UserDTO;
import dnsolutions.fi.SelfDevelopment.security.JwtService;
import dnsolutions.fi.SelfDevelopment.service.AuthService;
import dnsolutions.fi.SelfDevelopment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    public AuthResponseDTO register(AddUserRequestDTO addUserRequestDTO) {
        UserDTO user = userService.createNewUser(addUserRequestDTO);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return buildAuthResponse(userDetails, user);
    }

    @Override
    public AuthResponseDTO login(AuthLoginRequestDTO authLoginRequestDTO) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authLoginRequestDTO.getUsername(),
                authLoginRequestDTO.getPassword()
        ));

        UserDetails userDetails = userDetailsService.loadUserByUsername(authLoginRequestDTO.getUsername());
        UserDTO user = userService.getUserByUsername(authLoginRequestDTO.getUsername());

        return buildAuthResponse(userDetails, user);
    }

    private AuthResponseDTO buildAuthResponse(UserDetails userDetails, UserDTO user) {
        return new AuthResponseDTO(jwtService.generateToken(userDetails), "Bearer", user);
    }
}
