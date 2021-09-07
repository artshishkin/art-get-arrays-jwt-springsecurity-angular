package net.shyshkin.study.fullstack.supportportal.backend.service;

import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    User register(String firstName, String lastName, String username, String email);

    List<User> findAll();

    User findByUsername(String username);

    User findByEmail(String email);

}
