package net.shyshkin.study.fullstack.supportportal.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.shyshkin.study.fullstack.supportportal.backend.constant.FileConstant.*;

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
                .role(defaultRole)
                .build();
        return addNewUser(newUserDto);
    }

    private String generateProfileImageUrl(String userId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(DEFAULT_USER_IMAGE_PATH)
                .pathSegment(userId)
                .pathSegment(USER_IMAGE_FILENAME)
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

        User newUser = userMapper.toEntity(userDto);

        newUser.setPassword(encodedPassword);
        newUser.setUserId(generateUserId());
        newUser.setProfileImageUrl(generateProfileImageUrl(newUser.getUserId()));

        userRepository.save(newUser);
        saveProfileImage(newUser, userDto.getProfileImage());

        try {
            emailService.sendNewPasswordEmail(newUser.getFirstName(), rawPassword, newUser.getEmail());
        } catch (Exception exception) {
//            log.error("Can't send message", exception);
            log.debug("Can't send message. Error: {} ", exception.getMessage());
        }

        return newUser;
    }

    private void saveProfileImage(User user, MultipartFile profileImage) {
        if (profileImage == null) return;

        Path userFolder = Paths.get(USER_FOLDER, user.getUserId());
        try {
            if (Files.notExists(userFolder)) {
                Files.createDirectories(userFolder);
                log.debug(DIRECTORY_CREATED);
            }
            profileImage.transferTo(userFolder.resolve(USER_IMAGE_FILENAME));
            log.debug(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
        } catch (IOException exception) {
            log.error("Can't save to file", exception);
        }
    }

    @Override
    public User updateUser(String username, UserDto userDto) {

        String newUsername = userDto.getUsername();
        String email = userDto.getEmail();

        User user = validateUpdateUsernameAndEmail(username, newUsername, email);

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole().name());
        user.setAuthorities(userDto.getRole().getAuthorities());
        user.setNotLocked(userDto.isNonLocked());
        user.setActive(userDto.isActive());

        userRepository.save(user);
        saveProfileImage(user, userDto.getProfileImage());

        return user;
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void resetPassword(String email) {
        User user = findByEmail(email);
        String rawPassword = generatePassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        try {
            emailService.sendNewPasswordEmail(user.getFirstName(), rawPassword, user.getEmail());
        } catch (Exception exception) {
            log.debug("Can't send message. Error: {} ", exception.getMessage());
        }
    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage) {
        User user = findByUsername(username);
        saveProfileImage(user, profileImage);
        return user;
    }

    @Override
    public byte[] getProfileImage(String username) throws IOException {
        User user = findByUsername(username);
        return getImageByUserId(user.getUserId(), USER_IMAGE_FILENAME);
    }

    @Override
    public byte[] getImageByUserId(String userId, String filename) throws IOException {
        Path userProfileImagePath = Paths
                .get(USER_FOLDER, userId, filename);
        return Files.readAllBytes(userProfileImagePath);
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
