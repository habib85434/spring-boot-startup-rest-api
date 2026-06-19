package dnsolutions.fi.SelfDevelopment.exception;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(Long id) {
        super("Role not found with the ID: " + id);
    }
}
