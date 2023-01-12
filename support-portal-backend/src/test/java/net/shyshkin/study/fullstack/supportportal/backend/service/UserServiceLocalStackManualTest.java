package net.shyshkin.study.fullstack.supportportal.backend.service;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.common.BaseUserTest;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
@ActiveProfiles({"local", "image-s3-localstack"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Disabled("Only for manual testing. First start docker-compose")
class UserServiceLocalStackManualTest extends BaseUserTest {

    @Autowired
    UserService userService;

    @Test
    @Order(10)
    void updateProfileImage() {
        //given
        User fakeUser = createRandomUser();
        user = userRepository.save(fakeUser);
        UUID userId = user.getUserId();
        String filename = "avatar.jpg";

        //when
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.jpg",
                "image/jpeg", ("Spring Framework" + UUID.randomUUID()).getBytes());
        userService.updateProfileImage(userId, multipartFile);

        //then

        assertThat(user.getProfileImageUrl()).contains(userId + "/profile-image");
    }

    @Test
    @Order(20)
    void getImageByUserId() {
        //given
        UUID userId = user.getUserId();
        String filename = "avatar.jpg";

        //when
        byte[] imageByUserId = userService.getImageByUserId(userId, filename);

        //then
        assertAll(
                () -> assertThat(imageByUserId).isNotNull(),
                () -> {
                    String imageContent = new String(imageByUserId);
                    assertThat(imageContent).contains("Spring Framework");
                }
        );
    }
}