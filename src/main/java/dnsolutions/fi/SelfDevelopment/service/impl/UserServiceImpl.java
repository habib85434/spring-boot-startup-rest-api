package dnsolutions.fi.SelfDevelopment.service.impl;

import dnsolutions.fi.SelfDevelopment.dto.AddUserRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.RoleDTO;
import dnsolutions.fi.SelfDevelopment.dto.UserRoleRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.UserDTO;
import dnsolutions.fi.SelfDevelopment.entity.User;
import dnsolutions.fi.SelfDevelopment.entity.UserRole;
import dnsolutions.fi.SelfDevelopment.exception.BadRequestException;
import dnsolutions.fi.SelfDevelopment.exception.RoleNotFoundException;
import dnsolutions.fi.SelfDevelopment.exception.UserNotFoundException;
import dnsolutions.fi.SelfDevelopment.repository.UserRoleRepository;
import dnsolutions.fi.SelfDevelopment.repository.UserRepository;
import dnsolutions.fi.SelfDevelopment.service.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

import static dnsolutions.fi.SelfDevelopment.util.FieldUpdateUtils.readStringField;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._#-])[A-Za-z\\d@$!%*?&._#-]{8,100}$"
    );

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ModelMapper modelMapper;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserDTO)
                .toList();
    }

    @Override
    public UserDTO getUserById(Long id) {
        return mapToUserDTO(findUserById(id));
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        return mapToUserDTO(findUserByUsername(username));
    }

    @Override
    @Transactional
    public UserDTO createNewUser(AddUserRequestDTO addUserRequestDTO) {
        User userToSave = mapToUser(addUserRequestDTO);
        userToSave.setRoles(resolveRoles(addUserRequestDTO.getRoles()));
        validateUser(userToSave);

        User saveUser = userRepository.save(userToSave);
        return mapToUserDTO(saveUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, AddUserRequestDTO addUserRequestDTO) {
        User user = findUserById(id);
        user.setUsername(addUserRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(addUserRequestDTO.getPassword()));
        user.setEmail(addUserRequestDTO.getEmail());
        user.setFullName(addUserRequestDTO.getFullName());
        user.setRoles(resolveRoles(addUserRequestDTO.getRoles()));
        validateUser(user);

        User savedUser = userRepository.save(user);
        return mapToUserDTO(savedUser);
    }

    @Override
    @Transactional
    public UserDTO updatePartialUser(Long id, Map<String, Object> updates) {
        User user = findUserById(id);
        if (updates == null || updates.isEmpty()) {
            return mapToUserDTO(user);
        }

        updates.forEach((field, value) -> applyPartialUpdate(user, field, value));
        validateUser(user);

        User savedUser = userRepository.save(user);
        return mapToUserDTO(savedUser);
    }

    private UserDTO mapToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    private User mapToUser(AddUserRequestDTO addUserRequestDTO) {
        User user = new User();
        user.setUsername(addUserRequestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(addUserRequestDTO.getPassword()));
        user.setEmail(addUserRequestDTO.getEmail());
        user.setFullName(addUserRequestDTO.getFullName());
        return user;
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new UserNotFoundException(id));
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private void applyPartialUpdate(User user, String field, Object value) {
        switch (field) {
            case "username" -> user.setUsername(readStringField(field, value));
            case "password" -> user.setPassword(passwordEncoder.encode(readPasswordField(value)));
            case "email" -> user.setEmail(readStringField(field, value));
            case "fullName" -> user.setFullName(readStringField(field, value));
            case "roles" -> user.setRoles(resolveRolesFromPatch(value));
            case "id" -> throw new BadRequestException("User ID cannot be updated");
            default -> throw new BadRequestException("Unsupported user field: " + field);
        }
    }

    private Set<UserRole> resolveRoles(Set<UserRoleRequestDTO> roles) {
        if (roles == null || roles.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return roles.stream()
                .map(this::resolveRole)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private UserRole resolveRole(UserRoleRequestDTO roleDTO) {
        if (roleDTO == null || roleDTO.getId() == null) {
            throw new BadRequestException("Role cannot be null");
        }

        return userRoleRepository.findById(roleDTO.getId())
                .orElseThrow(() -> new RoleNotFoundException(roleDTO.getId()));
    }

    private Set<UserRole> resolveRolesFromPatch(Object value) {
        if (!(value instanceof Collection<?> roleValues)) {
            throw new BadRequestException("roles must be an array");
        }

        Set<UserRoleRequestDTO> roleDTOs = roleValues.stream()
                .map(this::mapPatchValueToUserRoleRequestDTO)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return resolveRoles(roleDTOs);
    }

    private UserRoleRequestDTO mapPatchValueToUserRoleRequestDTO(Object value) {
        if (!(value instanceof Map<?, ?> roleValue)) {
            throw new BadRequestException("Each role must be an object");
        }

        Object id = roleValue.get("id");
        if (id == null) {
            throw new BadRequestException("roles.id is required");
        }
        if (roleValue.containsKey("name")) {
            throw new BadRequestException("Use roles.id only. Create roles through /roles before assigning them to users");
        }
        return new UserRoleRequestDTO(readLongField("roles.id", id));
    }

    private Long readLongField(String field, Object value) {
        if (!(value instanceof Number numberValue)) {
            throw new BadRequestException(field + " must be a number");
        }
        return numberValue.longValue();
    }

    private String readPasswordField(Object value) {
        String password = readStringField("password", value);
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BadRequestException(
                    "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character"
            );
        }
        return password;
    }

    private void validateUser(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
