package dnsolutions.fi.SelfDevelopment.service.impl;

import dnsolutions.fi.SelfDevelopment.dto.AddRoleRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.RoleDTO;
import dnsolutions.fi.SelfDevelopment.entity.UserRole;
import dnsolutions.fi.SelfDevelopment.exception.BadRequestException;
import dnsolutions.fi.SelfDevelopment.exception.RoleNotFoundException;
import dnsolutions.fi.SelfDevelopment.repository.UserRoleRepository;
import dnsolutions.fi.SelfDevelopment.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<RoleDTO> getAllRoles() {
        return userRoleRepository.findAll()
                .stream()
                .map(this::mapToRoleDTO)
                .toList();
    }

    @Override
    public RoleDTO getRoleById(Long id) {
        return mapToRoleDTO(findRoleById(id));
    }

    @Override
    @Transactional
    public RoleDTO createRole(AddRoleRequestDTO addRoleRequestDTO) {
        validateRoleNameIsAvailable(addRoleRequestDTO.getName());
        UserRole roleToSave = mapToUserRole(addRoleRequestDTO);
        UserRole savedRole = userRoleRepository.save(roleToSave);
        return mapToRoleDTO(savedRole);
    }

    @Override
    @Transactional
    public RoleDTO updateRole(Long id, AddRoleRequestDTO addRoleRequestDTO) {
        UserRole role = findRoleById(id);

        if (!role.getName().equalsIgnoreCase(addRoleRequestDTO.getName())) {
            validateRoleNameIsAvailable(addRoleRequestDTO.getName());
        }

        role.setName(addRoleRequestDTO.getName());
        UserRole savedRole = userRoleRepository.save(role);
        return mapToRoleDTO(savedRole);
    }

    @Override
    @Transactional
    public void deleteRoleById(Long id) {
        UserRole role = findRoleById(id);
        userRoleRepository.delete(role);
    }

    private UserRole findRoleById(Long id) {
        return userRoleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
    }

    private void validateRoleNameIsAvailable(String name) {
        if (userRoleRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Role already exists with name: " + name);
        }
    }

    private UserRole mapToUserRole(AddRoleRequestDTO addRoleRequestDTO) {
        return modelMapper.map(addRoleRequestDTO, UserRole.class);
    }

    private RoleDTO mapToRoleDTO(UserRole userRole) {
        return modelMapper.map(userRole, RoleDTO.class);
    }
}
