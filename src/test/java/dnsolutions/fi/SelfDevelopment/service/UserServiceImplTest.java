package dnsolutions.fi.SelfDevelopment.service;

import dnsolutions.fi.SelfDevelopment.dto.AddUserRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.UserRoleRequestDTO;
import dnsolutions.fi.SelfDevelopment.entity.User;
import dnsolutions.fi.SelfDevelopment.entity.UserRole;
import dnsolutions.fi.SelfDevelopment.exception.BadRequestException;
import dnsolutions.fi.SelfDevelopment.exception.RoleNotFoundException;
import dnsolutions.fi.SelfDevelopment.exception.UserNotFoundException;
import dnsolutions.fi.SelfDevelopment.repository.UserRepository;
import dnsolutions.fi.SelfDevelopment.repository.UserRoleRepository;
import dnsolutions.fi.SelfDevelopment.service.impl.UserServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private Validator validator;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                userRoleRepository,
                new ModelMapper(),
                validator,
                passwordEncoder
        );
    }

    @Test
    void getAllUsers_ShouldReturnMappedUsers() {
        User user = createUser();
        user.setRoles(Set.of(new UserRole(1L, "USER")));
        when(userRepository.findAll()).thenReturn(List.of(user));

        var result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
        assertEquals("habibur", result.getFirst().getUsername());
        assertEquals("habib@dnsolutions.fi", result.getFirst().getEmail());
        assertEquals(1, result.getFirst().getRoles().size());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        var result = userService.getUserById(1L);

        assertEquals("habibur", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void getUserByUsername_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findByUsername("habibur")).thenReturn(Optional.of(createUser()));

        var result = userService.getUserByUsername("habibur");

        assertEquals("habibur", result.getUsername());
        verify(userRepository).findByUsername("habibur");
    }

    @Test
    void getUserByUsername_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername("missing"));
    }

    @Test
    void createNewUser_WithExistingRole_ShouldEncodePasswordAndSaveUser() {
        AddUserRequestDTO request = createRequest(Set.of(new UserRoleRequestDTO(1L)));
        UserRole role = new UserRole(1L, "USER");
        when(passwordEncoder.encode("Password@123")).thenReturn("hashed-password");
        when(userRoleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(validator.validate(any(User.class))).thenReturn(Set.of());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        var result = userService.createNewUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("hashed-password", savedUser.getPassword());
        assertEquals(Set.of(role), savedUser.getRoles());
        assertEquals("habibur", result.getUsername());
    }

    @Test
    void createNewUser_WithNoRoles_ShouldSaveUserWithEmptyRoleSet() {
        AddUserRequestDTO request = createRequest(Set.of());
        when(passwordEncoder.encode("Password@123")).thenReturn("hashed-password");
        when(validator.validate(any(User.class))).thenReturn(Set.of());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createNewUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().getRoles().isEmpty());
        verify(userRoleRepository, never()).findById(any());
    }

    @Test
    void createNewUser_WithNullRoles_ShouldSaveUserWithEmptyRoleSet() {
        AddUserRequestDTO request = createRequest(null);
        when(passwordEncoder.encode("Password@123")).thenReturn("hashed-password");
        when(validator.validate(any(User.class))).thenReturn(Set.of());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createNewUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().getRoles().isEmpty());
    }

    @Test
    void createNewUser_WithNullRoleItem_ShouldThrowBadRequest() {
        Set<UserRoleRequestDTO> roles = new LinkedHashSet<>();
        roles.add(null);
        AddUserRequestDTO request = createRequest(roles);
        when(passwordEncoder.encode("Password@123")).thenReturn("hashed-password");

        assertThrows(BadRequestException.class, () -> userService.createNewUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createNewUser_WithMissingRole_ShouldThrowRoleNotFound() {
        AddUserRequestDTO request = createRequest(Set.of(new UserRoleRequestDTO(99L)));
        when(passwordEncoder.encode("Password@123")).thenReturn("hashed-password");
        when(userRoleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> userService.createNewUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createNewUser_WhenValidationFails_ShouldThrowConstraintViolationException() {
        AddUserRequestDTO request = createRequest(Set.of());
        ConstraintViolation<User> violation = mock(ConstraintViolation.class);
        when(passwordEncoder.encode("Password@123")).thenReturn("hashed-password");
        when(validator.validate(any(User.class))).thenReturn(Set.of(violation));

        assertThrows(ConstraintViolationException.class, () -> userService.createNewUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        User user = createUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void updateUser_ShouldUpdateFieldsEncodePasswordAndRoles() {
        User user = createUser();
        UserRole admin = new UserRole(2L, "ADMIN");
        AddUserRequestDTO request = createRequest(Set.of(new UserRoleRequestDTO(2L)));
        request.setUsername("updated");
        request.setFullName("Updated User");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Password@123")).thenReturn("updated-hash");
        when(userRoleRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(validator.validate(any(User.class))).thenReturn(Set.of());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = userService.updateUser(1L, request);

        assertEquals("updated", result.getUsername());
        assertEquals("Updated User", result.getFullName());
        assertEquals("updated-hash", user.getPassword());
        assertEquals(Set.of(admin), user.getRoles());
    }

    @Test
    void updatePartialUser_WithNullUpdates_ShouldReturnExistingUserWithoutSaving() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        var result = userService.updatePartialUser(1L, null);

        assertEquals("habibur", result.getUsername());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePartialUser_WithEmptyUpdates_ShouldReturnExistingUserWithoutSaving() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        var result = userService.updatePartialUser(1L, Map.of());

        assertEquals("habibur", result.getUsername());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePartialUser_WithStringFields_ShouldUpdateAndSave() {
        User user = createUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(validator.validate(any(User.class))).thenReturn(Set.of());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = userService.updatePartialUser(1L, Map.of(
                "username", "newuser",
                "email", "new@dnsolutions.fi",
                "fullName", "New User"
        ));

        assertEquals("newuser", result.getUsername());
        assertEquals("new@dnsolutions.fi", result.getEmail());
        assertEquals("New User", result.getFullName());
        verify(userRepository).save(user);
    }

    @Test
    void updatePartialUser_WithPassword_ShouldValidateEncodeAndSavePassword() {
        User user = createUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPassword@123")).thenReturn("new-password-hash");
        when(validator.validate(any(User.class))).thenReturn(Set.of());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.updatePartialUser(1L, Map.of("password", "NewPassword@123"));

        assertEquals("new-password-hash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void updatePartialUser_WithRoles_ShouldReplaceRolesById() {
        User user = createUser();
        UserRole role = new UserRole(1L, "USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRoleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(validator.validate(any(User.class))).thenReturn(Set.of());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = userService.updatePartialUser(1L, Map.of(
                "roles", List.of(Map.of("id", 1))
        ));

        assertEquals(1, result.getRoles().size());
        assertEquals(Set.of(role), user.getRoles());
    }

    @Test
    void updatePartialUser_WithUnsupportedField_ShouldThrowBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        assertThrows(BadRequestException.class, () -> userService.updatePartialUser(1L, Map.of("unknown", "value")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePartialUser_WithIdField_ShouldThrowBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        assertThrows(BadRequestException.class, () -> userService.updatePartialUser(1L, Map.of("id", 2)));
    }

    @Test
    void updatePartialUser_WithNonStringFieldValue_ShouldThrowBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        assertThrows(BadRequestException.class, () -> userService.updatePartialUser(1L, Map.of("email", 123)));
    }

    @Test
    void updatePartialUser_WithInvalidPassword_ShouldThrowBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        assertThrows(BadRequestException.class, () -> userService.updatePartialUser(1L, Map.of("password", "weak")));
    }

    @Test
    void updatePartialUser_WithNonArrayRoles_ShouldThrowBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        assertThrows(BadRequestException.class, () -> userService.updatePartialUser(1L, Map.of("roles", 1)));
    }

    @Test
    void updatePartialUser_WithNonObjectRole_ShouldThrowBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        assertThrows(BadRequestException.class, () -> userService.updatePartialUser(1L, Map.of(
                "roles", List.of(1)
        )));
    }

    @Test
    void updatePartialUser_WithRoleName_ShouldThrowBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        assertThrows(BadRequestException.class, () -> userService.updatePartialUser(1L, Map.of(
                "roles", List.of(Map.of("id", 1, "name", "USER"))
        )));
    }

    @Test
    void updatePartialUser_WithMissingRoleId_ShouldThrowBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        assertThrows(BadRequestException.class, () -> userService.updatePartialUser(1L, Map.of(
                "roles", List.of(Map.of("name", "USER"))
        )));
    }

    @Test
    void updatePartialUser_WithNonNumericRoleId_ShouldThrowBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));

        assertThrows(BadRequestException.class, () -> userService.updatePartialUser(1L, Map.of(
                "roles", List.of(Map.of("id", "1"))
        )));
    }

    @Test
    void updatePartialUser_WithMissingRole_ShouldThrowRoleNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));
        when(userRoleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> userService.updatePartialUser(1L, Map.of(
                "roles", List.of(Map.of("id", 99L))
        )));
    }

    @Test
    void updatePartialUser_WhenValidationFails_ShouldThrowConstraintViolationException() {
        ConstraintViolation<User> violation = mock(ConstraintViolation.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(createUser()));
        when(validator.validate(any(User.class))).thenReturn(Set.of(violation));

        assertThrows(ConstraintViolationException.class, () -> userService.updatePartialUser(1L, Map.of(
                "email", "invalid-email"
        )));
        verify(userRepository, never()).save(any());
    }

    @Test
    void exceptions_ShouldExposeExpectedRuntimeTypes() {
        assertInstanceOf(RuntimeException.class, new BadRequestException("bad"));
        assertInstanceOf(RuntimeException.class, new UserNotFoundException(1L));
        assertInstanceOf(RuntimeException.class, new UserNotFoundException("habibur"));
        assertInstanceOf(RuntimeException.class, new RoleNotFoundException(1L));
    }

    private AddUserRequestDTO createRequest(Set<UserRoleRequestDTO> roles) {
        return new AddUserRequestDTO(
                "habibur",
                "Password@123",
                "habib@dnsolutions.fi",
                "Habibur Rahman",
                roles
        );
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("habibur");
        user.setPassword("existing-hash");
        user.setEmail("habib@dnsolutions.fi");
        user.setFullName("Habibur Rahman");
        return user;
    }
}
