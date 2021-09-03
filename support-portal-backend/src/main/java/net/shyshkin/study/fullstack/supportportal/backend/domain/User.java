package net.shyshkin.study.fullstack.supportportal.backend.domain;

import java.io.Serializable;
import java.time.LocalDateTime;


public class User implements Serializable {

    private static final long serialVersionUID = -4372214856545239049L;

    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String profileImageUrl;
    private LocalDateTime lastLoginDate;
    private LocalDateTime lastLoginDateDisplay;
    private LocalDateTime joinDate;
    private String[] roles; //ROLE_USER, ROLE_ADMIN
    private String[] authorities;
    private boolean isActive;
    private boolean isNotLocked;

}
