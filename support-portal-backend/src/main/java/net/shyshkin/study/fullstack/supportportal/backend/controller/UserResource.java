package net.shyshkin.study.fullstack.supportportal.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.constant.SecurityConstants;
import net.shyshkin.study.fullstack.supportportal.backend.domain.HttpResponse;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.domain.UserPrincipal;
import net.shyshkin.study.fullstack.supportportal.backend.domain.dto.UserDto;
import net.shyshkin.study.fullstack.supportportal.backend.service.UserService;
import net.shyshkin.study.fullstack.supportportal.backend.utility.JwtTokenProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("user")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200")
public class UserResource {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("home")
    public String showUser() {
        return "Application works";
    }

    @PostMapping("register")
    public User register(@RequestBody User user) {
        return userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
    }

    @PostMapping("login")
    @CrossOrigin(value = "http://localhost:4200", exposedHeaders = {SecurityConstants.JWT_TOKEN_HEADER})
    public ResponseEntity<User> login(@RequestBody User user) {

        authenticate(user.getUsername(), user.getPassword());
        User byUsername = userService.findByUsername(user.getUsername());
        UserDetails userDetails = new UserPrincipal(byUsername);

        return ResponseEntity.ok()
                .header(SecurityConstants.JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userDetails))
                .body(byUsername);
    }

    @PostMapping("add")
    public User addNewUser(@Valid UserDto userDto) {
        log.debug("User DTO: {}", userDto);
        return userService.addNewUser(userDto);
    }

    @PutMapping("{currentUsername}")
    public User updateUser(@PathVariable String currentUsername, @Valid UserDto userDto) {
        log.debug("User DTO: {}", userDto);
        return userService.updateUser(currentUsername, userDto);
    }

    @GetMapping("{username}")
    public User findUser(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    @GetMapping
    public Page<User> getAllUsers(Pageable pageable) {
        return userService.findAll(pageable);
    }

    @PostMapping("/resetPassword/{email}")
    public HttpResponse resetPassword(@PathVariable String email) {
        userService.resetPassword(email);
        return HttpResponse.builder()
                .httpStatusCode(OK.value())
                .httpStatus(OK)
                .reason(OK.getReasonPhrase())
                .message("Password reset successfully. Check your email for new password")
                .build();
    }

    @DeleteMapping("{userId}")
    @PreAuthorize("hasAuthority('user:delete')")
    public HttpResponse deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return HttpResponse.builder()
                .httpStatusCode(OK.value())
                .httpStatus(OK)
                .reason(OK.getReasonPhrase())
                .message("User deleted successfully".toUpperCase())
                .build();
    }

    @PutMapping("{username}/profileImage")
    public User updateProfileImage(@PathVariable String username, MultipartFile profileImage) {
        return userService.updateProfileImage(username, profileImage);
    }

    @GetMapping(path = "{username}/image/profile", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable String username) throws IOException {
        byte[] profileImage = userService.getProfileImage(username);
        log.debug("File size: {}", profileImage.length);
        return profileImage;
    }

    @GetMapping(path = "image/profile/{userId}/{filename}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getProfileImageByUserId(@PathVariable String userId, @PathVariable String filename) throws IOException {
        byte[] profileImage = userService.getImageByUserId(userId, filename);
        log.debug("File size: {}", profileImage.length);
        return profileImage;
    }

    @GetMapping(path = "image/profile/{userId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getDefaultProfileImage(@PathVariable String userId) {
        byte[] profileImage = userService.getDefaultProfileImage(userId);
        log.debug("File size: {}", profileImage.length);
        return profileImage;
    }

    private void authenticate(String username, String password) {
        Authentication auth = new UsernamePasswordAuthenticationToken(username, password);
        authenticationManager.authenticate(auth);
    }

}
