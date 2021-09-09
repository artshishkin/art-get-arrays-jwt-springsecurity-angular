package net.shyshkin.study.fullstack.supportportal.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.constant.FileConstant;
import net.shyshkin.study.fullstack.supportportal.backend.domain.Role;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.UserPrincipal;
import net.shyshkin.study.fullstack.supportportal.backend.domain.dto.UserDto;
import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.EmailExistsException;
import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.EmailNotFoundException;
import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.UserNotFoundException;
import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.UsernameExistsException;
import net.shyshkin.study.fullstack.supportportal.backend.mapper.UserMapper;
import net.shyshkin.study.fullstack.supportportal.backend.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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

    public static final String USERNAME_NOT_FOUND_MSG = "User with username `%s` not found";
    public static final String USERNAME_EXISTS_MSG = "Username `%s` is already taken. Please select another one";
    public static final String EMAIL_NOT_FOUND_MSG = "User with email `%s` not found";
    public static final String EMAIL_EXISTS_MSG = "User with email `%s` is already registered";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final EmailService emailService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(USERNAME_NOT_FOUND_MSG, username)));
        validateLoginAttempts(user);
        user.setLastLoginDateDisplay(user.getLastLoginDate());
        user.setLastLoginDate(LocalDateTime.now());
        return new UserPrincipal(user);
    }

    private void validateLoginAttempts(User user) {
        if (user.isNotLocked()) {
            if (loginAttemptService.hasExceededMaxAttempts(user.getUsername()))
                user.setNotLocked(false);
        } else {
            loginAttemptService.evictUserFromCache(user.getUsername());
        }
    }

    @Override
    public User register(String firstName, String lastName, String username, String email) {

        Role defaultRole = Role.ROLE_USER;

        UserDto newUserDto = UserDto.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .isActive(true)
                .isNonLocked(true)
                .role(defaultRole.name())
                .build();
        return addNewUser(newUserDto);
    }

    private String getTemporaryProfileImageUrl(String username) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(FileConstant.DEFAULT_USER_IMAGE_PATH)
                .pathSegment(username)
                .toUriString();
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

    @Override
    public User addNewUser(UserDto userDto) {

        String username = userDto.getUsername();
        String email = userDto.getEmail();

        validateNewUsernameAndEmail(username, email);

        String rawPassword = generatePassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Role role = Role.valueOf(userDto.getRole());

        User newUser = userMapper.toEntity(userDto);

        newUser.setPassword(encodedPassword);
        newUser.setUserId(generateUserId());
        newUser.setProfileImageUrl(getTemporaryProfileImageUrl(username));
        newUser.setRole(role.name());
        newUser.setAuthorities(role.getAuthorities());

        userRepository.save(newUser);
        saveProfileImage(newUser, userDto.getProfileImage());

        try {
            emailService.sendNewPasswordEmail(newUser.getFirstName(), rawPassword, newUser.getEmail());
        } catch (Exception exception) {
            log.error("Can't send message", exception);
        }

        return newUser;
    }

    private void saveProfileImage(User user, MultipartFile profileImage) {
        if (profileImage == null) return;
    }

    @Override
    public User updateUser(String username, UserDto userDto) {
        return null;
    }

    @Override
    public void deleteUser(long id) {

    }

    @Override
    public void resetPassword(String email) {

    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage) {
        return null;
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
