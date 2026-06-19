package dnsolutions.fi.SelfDevelopment.repository;

import dnsolutions.fi.SelfDevelopment.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        User savedUser = userRepository.save(createUser(uniqueUsername("repo"), uniqueEmail("repo")));

        var result = userRepository.findByUsername(savedUser.getUsername());

        assertTrue(result.isPresent());
        assertEquals(savedUser.getId(), result.get().getId());
        assertEquals(savedUser.getEmail(), result.get().getEmail());
    }

    @Test
    void findByUsername_WhenUserDoesNotExist_ShouldReturnEmptyOptional() {
        var result = userRepository.findByUsername("missing-user-" + UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void save_WithDuplicateUsername_ShouldThrowDataIntegrityViolationException() {
        String username = uniqueUsername("dup");
        userRepository.saveAndFlush(createUser(username, uniqueEmail("first")));

        User duplicateUser = createUser(username, uniqueEmail("second"));

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(duplicateUser));
    }

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("$2a$10$abcdefghijklmnopqrstuuCwP5bN2AX7TC3QASCNMmdYt1hWqR8U2");
        user.setEmail(email);
        user.setFullName("Repository User");
        return user;
    }

    private String uniqueUsername(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 12);
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@dnsolutions.fi";
    }
}
