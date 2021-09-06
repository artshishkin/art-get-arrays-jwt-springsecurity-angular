package net.shyshkin.study.fullstack.supportportal.backend.controller;

import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.EmailExistsException;
import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.UserNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exceptions")
public class ExceptionsController {

    @GetMapping("/emailExists")
    public String emailExistsException() throws EmailExistsException {
        throw new EmailExistsException("This email is already taken");
    }

    @GetMapping("/userNotFound")
    public String userNotFoundException() throws  UserNotFoundException {
        throw new UserNotFoundException("The user was not found");
    }
}
