package dnsolutions.fi.SelfDevelopment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddRoleRequestDTO {

    @NotBlank(message = "Role can not be empty")
    @Size(min = 3, max = 50)
    private String name;
}
