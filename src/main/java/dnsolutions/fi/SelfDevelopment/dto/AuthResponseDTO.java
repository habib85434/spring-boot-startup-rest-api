package dnsolutions.fi.SelfDevelopment.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String accessToken;
    private String tokenType;
    private UserDTO user;
    @JsonIgnore
    private String refreshToken;

    public AuthResponseDTO(String accessToken, String tokenType, UserDTO user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.user = user;
    }
}
