package net.shyshkin.study.fullstack.supportportal.backend.repository;

import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findAllByEmail(String email);

}