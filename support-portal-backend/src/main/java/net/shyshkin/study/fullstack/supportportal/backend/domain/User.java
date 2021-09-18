package net.shyshkin.study.fullstack.supportportal.backend.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class User implements Serializable {

    private static final long serialVersionUID = -4372214856545239049L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long id;

    private String userId;
    private String firstName;
    private String lastName;
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String email;
    private String profileImageUrl;
    private LocalDateTime lastLoginDate;
    private LocalDateTime lastLoginDateDisplay;
    private LocalDateTime joinDate;
    private String role; //ROLE_USER, ROLE_ADMIN
    private String[] authorities;
    private boolean isActive;
    private boolean isNotLocked;

}
