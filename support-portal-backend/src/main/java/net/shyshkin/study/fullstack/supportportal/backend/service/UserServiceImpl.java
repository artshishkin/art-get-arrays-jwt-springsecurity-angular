package net.shyshkin.study.fullstack.supportportal.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.domain.Role;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.UserPrincipal;
import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.EmailExistsException;
import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.EmailNotFoundException;
import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.UserNotFoundException;
import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.UsernameExistsException;
import net.shyshkin.study.fullstack.supportportal.backend.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public static final String DEFAULT_USER_IMG_PATH = "/user/image/profile/temp";
    public static final String USERNAME_NOT_FOUND_MSG = "User with username `%s` not found";
    public static final String USERNAME_EXISTS_MSG = "Username `%s` is already taken. Please select another one";
    public static final String EMAIL_NOT_FOUND_MSG = "User with email `%s` not found";
    public static final String EMAIL_EXISTS_MSG = "User with email `%s` is already registered";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(USERNAME_NOT_FOUND_MSG, username)));
        user.setLastLoginDateDisplay(user.getLastLoginDate());
        user.setLastLoginDate(LocalDateTime.now());
        return new UserPrincipal(user);
    }

    @Override
    public User register(String firstName, String lastName, String username, String email) {

        validateNewUsernameAndEmail(username, email);

        Role defaultRole = Role.ROLE_USER;

        String rawPassword = generatePassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        log.debug("Raw password: {}. Encoded password: {}", rawPassword, encodedPassword);

        User newUser = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password(encodedPassword)
                .userId(generateUserId())
                .isActive(true)
                .isNotLocked(true)
                .joinDate(LocalDateTime.now())
                .profileImageUrl(getTemporaryProfileImageUrl())
                .lastLoginDate(null)
                .lastLoginDateDisplay(null)
                .role(defaultRole.name())
                .authorities(defaultRole.getAuthorities())
                .build();
        return userRepository.save(newUser);
    }

    private String getTemporaryProfileImageUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMG_PATH).build().toString();
    }

    private String generatePassword() {
        return RandomStringUtils.randomAscii(10);
    }

    private String generateUserId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(String.format(USERNAME_NOT_FOUND_MSG, username)));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException(String.format(EMAIL_NOT_FOUND_MSG, email)));
    }

    private void validateNewUsernameAndEmail(String username, String email) {

        if (userRepository.existsByUsername(username))
            throwUsernameExistsException(username);

        if (userRepository.existsByEmail(email))
            throwEmailExistsException(email);
    }

    private User validateUpdateUsernameAndEmail(String currentUsername, String username, String email) {

        Objects.requireNonNull(currentUsername);

        User currentUser = findByUsername(currentUsername);

        if (!Objects.equals(currentUsername, username) && userRepository.existsByUsername(username))
            throwUsernameExistsException(username);

        if (!Objects.equals(currentUser.getEmail(), email) && userRepository.existsByEmail(email))
            throwEmailExistsException(email);

        return currentUser;
    }

    private void throwEmailExistsException(String email) {
        throw new EmailExistsException(String.format(EMAIL_EXISTS_MSG, email));
    }

    private void throwUsernameExistsException(String username) {
        throw new UsernameExistsException(String.format(USERNAME_EXISTS_MSG, username));
    }
}
