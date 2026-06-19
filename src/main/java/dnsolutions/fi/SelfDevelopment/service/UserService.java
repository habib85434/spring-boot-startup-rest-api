package dnsolutions.fi.SelfDevelopment.service;

import dnsolutions.fi.SelfDevelopment.dto.AddUserRequestDTO;
import dnsolutions.fi.SelfDevelopment.dto.UserDTO;

import java.util.List;
import java.util.Map;

public interface UserService {

    List<UserDTO> getAllUsers();

    UserDTO getUserById(Long id);

    UserDTO getUserByUsername(String username);

    UserDTO createNewUser(AddUserRequestDTO addUserRequestDTO);

    void deleteUser(Long id);

    UserDTO updateUser(Long id, AddUserRequestDTO addUserRequestDTO);

    UserDTO updatePartialUser(Long id, Map<String, Object> updates);

}
