package dnsolutions.fi.SelfDevelopment.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddUserRequestDTO {

    @NotBlank(message = "Username can not be empty")
    @Size(min = 5, max = 50)
    private String username;

    @NotBlank(message = "Password can not be empty")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._#-])[A-Za-z\\d@$!%*?&._#-]{8,100}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character"
    )
    private String password;

    @NotBlank(message = "Email can not be empty")
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Full Name can not be empty")
    @Size(min = 2, max = 150)
    private String fullName;

    @Valid
    private Set<UserRoleRequestDTO> roles = new HashSet<>();
}
