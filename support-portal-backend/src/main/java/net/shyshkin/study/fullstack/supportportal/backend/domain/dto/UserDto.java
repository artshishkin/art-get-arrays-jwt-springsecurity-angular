package net.shyshkin.study.fullstack.supportportal.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.shyshkin.study.fullstack.supportportal.backend.domain.Role;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    @NotEmpty(message = "Should not be empty")
    private String firstName;
    @NotEmpty(message = "Should not be empty")
    private String lastName;
    @NotEmpty(message = "Should not be empty")
    private String username;
    @NotEmpty(message = "Should not be empty")
    @Email(message = "Must match email format")
    private String email;
    @NotNull(message = "Role is mandatory")
    private Role role;
    private boolean isNotLocked;
    private boolean isActive;
    private MultipartFile profileImage;
}
