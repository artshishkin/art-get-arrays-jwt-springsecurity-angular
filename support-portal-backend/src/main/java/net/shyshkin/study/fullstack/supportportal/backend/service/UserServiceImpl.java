package net.shyshkin.study.fullstack.supportportal.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.domain.Role;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.UserPrincipal;
import net.shyshkin.study.fullstack.supportportal.backend.domain.dto.UserDto;
import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.*;
import net.shyshkin.study.fullstack.supportportal.backend.mapper.UserMapper;
import net.shyshkin.study.fullstack.supportportal.backend.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.RequestEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.shyshkin.study.fullstack.supportportal.backend.constant.FileConstant.*;
import static org.springframework.http.MediaType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    public static final String USERNAME_NOT_FOUND_MSG = "User with username `%s` not found";
    public static final String USER_NOT_FOUND_MSG = "User not found";
    public static final String USERNAME_EXISTS_MSG = "Username `%s` is already taken. Please select another one";
    public static final String EMAIL_NOT_FOUND_MSG = "User with email `%s` not found";
    public static final String EMAIL_EXISTS_MSG = "User with email `%s` is already registered";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final RestTemplateBuilder restTemplateBuilder;
    private final ProfileImageService profileImageService;

    private RestTemplate restTemplate;

    @PostConstruct
    void init() {
        restTemplate = restTemplateBuilder
                .rootUri(TEMP_PROFILE_IMAGE_BASE_URL)
                .build();
    }

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
                .isNotLocked(true)
                .role(defaultRole)
                .build();
        return addNewUser(newUserDto);
    }

    private String generateDefaultProfileImageUrl(UUID userId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(String.format(DEFAULT_USER_IMAGE_URI_PATTERN, userId))
                .toUriString();
    }

    private String generateProfileImageUrl(UUID userId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(String.format(DEFAULT_USER_IMAGE_URI_PATTERN, userId))
                .pathSegment(USER_IMAGE_FILENAME)
                .toUriString();
    }

    private String generatePassword() {
        return RandomStringUtils.randomAscii(10);
    }

    private UUID generateUserId() {
        return UUID.randomUUID();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
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
    public User findByUserId(UUID userId) {
        return userRepository
                .findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_MSG));
    }

    @Override
    public User addNewUser(UserDto userDto) {

        String username = userDto.getUsername();
        String email = userDto.getEmail();

        validateNewUsernameAndEmail(username, email);

        String rawPassword = generatePassword();
        log.debug("Added `{}` with Raw Password: {}", username, rawPassword);
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User newUser = userMapper.toEntity(userDto);

        newUser.setPassword(encodedPassword);
        newUser.setUserId(generateUserId());
        newUser.setProfileImageUrl(generateDefaultProfileImageUrl(newUser.getUserId()));

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

        if (!List.of(IMAGE_JPEG_VALUE, IMAGE_GIF_VALUE, IMAGE_PNG_VALUE).contains(profileImage.getContentType())) {
            throw new NotAnImageFileException(profileImage.getOriginalFilename() + " is not an image file. Please upload an image");
        }

        String imageUrl = profileImageService.persistProfileImage(user.getUserId(), profileImage, USER_IMAGE_FILENAME);

        if (imageUrl == null)
            imageUrl = generateProfileImageUrl(user.getUserId());

        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);
    }

    private void clearUserStorage(User user) {
        profileImageService.clearUserStorage(user.getUserId());
    }

    @Override
    public User updateUser(UUID userId, UserDto userDto) {

        String newUsername = userDto.getUsername();
        String email = userDto.getEmail();

        User user = validateUpdateUsernameAndEmail(userId, newUsername, email);

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole().name());
        user.setAuthorities(userDto.getRole().getAuthorities());
        user.setNotLocked(userDto.isNotLocked());
        user.setActive(userDto.isActive());

        userRepository.save(user);
        saveProfileImage(user, userDto.getProfileImage());

        return user;
    }

    @Override
    public void deleteUser(UUID userId) {
        User userToBeDeleted = userRepository
                .findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User was not found"));

        clearUserStorage(userToBeDeleted);
        userRepository.delete(userToBeDeleted);
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
    public User updateProfileImage(UUID userId, MultipartFile profileImage) {
        User user = findByUserId(userId);
        saveProfileImage(user, profileImage);
        return user;
    }

    @Override
    public byte[] getImageByUserId(UUID userId, String filename) {

        if (!userRepository.existsByUserId(userId)) {
            throw new UserNotFoundException(USER_NOT_FOUND_MSG);
        }
        return profileImageService.retrieveProfileImage(userId, filename);
    }

    @Override
    public byte[] getDefaultProfileImage(UUID userId) {

        if (!userRepository.existsByUserId(userId)) {
            throw new UserNotFoundException(USER_NOT_FOUND_MSG);
        }

//        "https://robohash.org/11951691-d373-4126-bef2-84d157a6546b"
        RequestEntity<Void> requestEntity = RequestEntity
                .get("/{userId}", userId)
                .accept(IMAGE_JPEG)
                .build();
        var responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<byte[]>() {
        });
        return responseEntity.getBody();
    }

    private void validateNewUsernameAndEmail(String username, String email) {

        if (userRepository.existsByUsername(username))
            throwUsernameExistsException(username);

        if (userRepository.existsByEmail(email))
            throwEmailExistsException(email);
    }

    private User validateUpdateUsernameAndEmail(UUID userId, String username, String email) {

        Objects.requireNonNull(userId);

        User currentUser = findByUserId(userId);

        if (!Objects.equals(currentUser.getUsername(), username) && userRepository.existsByUsername(username))
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
