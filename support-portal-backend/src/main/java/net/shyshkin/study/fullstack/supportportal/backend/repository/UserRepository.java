package net.shyshkin.study.fullstack.supportportal.backend.repository;

import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Boolean existsByUserId(UUID userId);

    Optional<User> findByUserId(UUID userId);

}
