package net.shyshkin.study.fullstack.supportportal.backend.repository;

import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
