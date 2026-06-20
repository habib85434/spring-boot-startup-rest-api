package dnsolutions.fi.SelfDevelopment.service;

import dnsolutions.fi.SelfDevelopment.dto.AddUserRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.AuthLoginRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO register(AddUserRequestDTO addUserRequestDTO);

    AuthResponseDTO login(AuthLoginRequestDTO authLoginRequestDTO);

    void logout(String accessToken);
}
