package net.shyshkin.study.fullstack.supportportal.backend.service;

import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService extends UserDetailsService {

    User register(String firstName, String lastName, String username, String email);

    List<User> findAll();

    User findByUsername(String username);

    User findByEmail(String email);

    User addNewUser(UserDto userDto);

    User updateUser(String username, UserDto userDto);

    void deleteUser(long id);

    void resetPassword(String email);

    User updateProfileImage(String username, MultipartFile profileImage);

    byte[] getProfileImage(String username) throws IOException;

}
