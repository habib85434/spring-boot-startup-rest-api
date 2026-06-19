package dnsolutions.fi.SelfDevelopment.repository;

import dnsolutions.fi.SelfDevelopment.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    boolean existsByNameIgnoreCase(String name);

    Optional<UserRole> findByNameIgnoreCase(String name);
}
