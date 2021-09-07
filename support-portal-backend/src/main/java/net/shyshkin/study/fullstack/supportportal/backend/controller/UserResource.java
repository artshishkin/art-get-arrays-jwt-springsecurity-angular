package net.shyshkin.study.fullstack.supportportal.backend.controller;

import lombok.RequiredArgsConstructor;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import net.shyshkin.study.fullstack.supportportal.backend.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserResource {

    private final UserService userService;

    @GetMapping("home")
    public String showUser() {
        return "Application works";
    }

    @PostMapping("register")
    public User register(@RequestBody User user) {
        return userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail());
    }
}
