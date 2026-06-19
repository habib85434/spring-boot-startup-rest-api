package dnsolutions.fi.SelfDevelopment.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("User not found with the ID: " + id);
    }

    public UserNotFoundException(String username) {
        super("User not found with the username: " + username);
    }
}
