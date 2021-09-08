package net.shyshkin.study.fullstack.supportportal.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String role;
    private boolean isNonLocked;
    private boolean isActive;
    private MultipartFile profileImage;
}
