package dnsolutions.fi.SelfDevelopment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dnsolutions.fi.SelfDevelopment.dto.AddUserRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.RoleDTO;
import dnsolutions.fi.SelfDevelopment.dto.UserDTO;
import dnsolutions.fi.SelfDevelopment.dto.UserRoleRequestDTO;
import dnsolutions.fi.SelfDevelopment.exception.GlobalExceptionHandler;
import dnsolutions.fi.SelfDevelopment.exception.UserNotFoundException;
import dnsolutions.fi.SelfDevelopment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllUsers_ShouldReturnUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(createUserDTO()));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("habibur"))
                .andExpect(jsonPath("$[0].roles[0].name").value("USER"));

        verify(userService).getAllUsers();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(createUserDTO());

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("habibur"));

        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(get("/users/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with the ID: 99"));
    }

    @Test
    void createNewUser_WithValidRequest_ShouldReturnCreatedUser() throws Exception {
        AddUserRequestDTO request = createRequest();
        when(userService.createNewUser(any(AddUserRequestDTO.class))).thenReturn(createUserDTO());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("habibur"));

        verify(userService).createNewUser(any(AddUserRequestDTO.class));
    }

    @Test
    void createNewUser_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        AddUserRequestDTO request = createRequest();
        request.setUsername("abc");
        request.setEmail("invalid-email");
        request.setPassword("weak");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.username").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void updateUser_WithValidRequest_ShouldReturnUpdatedUser() throws Exception {
        AddUserRequestDTO request = createRequest();
        UserDTO response = createUserDTO();
        response.setFullName("Updated User");
        when(userService.updateUser(eq(1L), any(AddUserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(put("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Updated User"));

        verify(userService).updateUser(eq(1L), any(AddUserRequestDTO.class));
    }

    @Test
    void updatePartialUser_ShouldReturnUpdatedUser() throws Exception {
        UserDTO response = createUserDTO();
        response.setEmail("new@dnsolutions.fi");
        when(userService.updatePartialUser(eq(1L), any(Map.class))).thenReturn(response);

        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "new@dnsolutions.fi"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@dnsolutions.fi"));

        verify(userService).updatePartialUser(eq(1L), any(Map.class));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    private AddUserRequestDTO createRequest() {
        return new AddUserRequestDTO(
                "habibur",
                "Password@123",
                "habib@dnsolutions.fi",
                "Habibur Rahman",
                Set.of(new UserRoleRequestDTO(1L))
        );
    }

    private UserDTO createUserDTO() {
        return new UserDTO(
                1L,
                "habibur",
                "habib@dnsolutions.fi",
                "Habibur Rahman",
                Set.of(new RoleDTO(1L, "USER"))
        );
    }
}
