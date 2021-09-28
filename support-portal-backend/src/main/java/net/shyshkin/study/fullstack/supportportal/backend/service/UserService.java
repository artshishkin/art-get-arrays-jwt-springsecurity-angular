package net.shyshkin.study.fullstack.supportportal.backend.service;

import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    User register(String firstName, String lastName, String username, String email);

    Page<User> findAll(Pageable pageable);

    User findByUsername(String username);

    User findByEmail(String email);

    User findByUserId(UUID userId);

    User addNewUser(UserDto userDto);

    User updateUser(UUID userId, UserDto userDto);

    void deleteUser(UUID userId);

    void resetPassword(String email);

    User updateProfileImage(UUID userId, MultipartFile profileImage);

    byte[] getImageByUserId(UUID userId, String filename) throws IOException;

    byte[] getDefaultProfileImage(UUID userId);
}
