package dnsolutions.fi.SelfDevelopment.service;

import dnsolutions.fi.SelfDevelopment.dto.AddRoleRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.RoleDTO;

import java.util.List;

public interface UserRoleService {
    List<RoleDTO> getAllRoles();

    RoleDTO getRoleById(Long id);

    RoleDTO createRole(AddRoleRequestDTO addRoleRequestDTO);

    RoleDTO updateRole(Long id, AddRoleRequestDTO addRoleRequestDTO);

    void deleteRoleById(Long id);
}
