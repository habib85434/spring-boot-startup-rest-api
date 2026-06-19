package dnsolutions.fi.SelfDevelopment.controller;

import dnsolutions.fi.SelfDevelopment.dto.AddRoleRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.RoleDTO;
import dnsolutions.fi.SelfDevelopment.service.UserRoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/roles")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(userRoleService.getAllRoles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(userRoleService.getRoleById(id));
    }

    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody AddRoleRequestDTO addRoleRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userRoleService.createRole(addRoleRequestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRole(
            @PathVariable @Positive Long id,
            @Valid @RequestBody AddRoleRequestDTO addRoleRequestDTO
    ) {
        return ResponseEntity.ok(userRoleService.updateRole(id, addRoleRequestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable @Positive Long id) {
        userRoleService.deleteRoleById(id);
        return ResponseEntity.noContent().build();
    }
}
