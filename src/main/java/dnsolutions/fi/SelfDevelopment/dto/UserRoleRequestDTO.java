package dnsolutions.fi.SelfDevelopment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleRequestDTO {

    @NotNull(message = "Role ID is required")
    @Positive(message = "Role ID must be positive")
    private Long id;
}
