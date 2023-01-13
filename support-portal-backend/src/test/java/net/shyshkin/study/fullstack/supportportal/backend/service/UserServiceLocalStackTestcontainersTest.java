package net.shyshkin.study.fullstack.supportportal.backend.service;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.common.BaseUserTest;
import net.shyshkin.study.fullstack.supportportal.backend.domain.User;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
@ActiveProfiles({"local", "image-s3-localstack"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
class UserServiceLocalStackTestcontainersTest extends BaseUserTest {

    private static final int S3_PORT = 4566;
    private static final String INIT_S3 = "/etc/localstack/init/ready.d/init-s3-bucket.sh";
    private static final String INIT_S3_HOST_LOCATION = "./../docker-compose/support-portal/init-s3-bucket.sh";

    @Autowired
    UserService userService;

    @Container
    static GenericContainer<?> s3LocalStack = new GenericContainer<>(DockerImageName.parse("localstack/localstack"))
            .withEnv(Map.of(
                    "DEBUG", "1",
                    "USE_SSL", "0",
                    "AWS_CBOR_DISABLE", "1",
                    "HOSTNAME", "localstack",
                    "SERVICES", "s3",
                    "AWS_DEFAULT_REGION", "eu-north-1"
            ))
            .withExposedPorts(S3_PORT)
            .withCopyFileToContainer(MountableFile.forHostPath(INIT_S3_HOST_LOCATION), INIT_S3);

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

    @DynamicPropertySource
    static void s3Properties(DynamicPropertyRegistry registry) {
        registry.add("config.aws.s3.url",
                () -> String.format("http://127.0.0.1:%s", s3LocalStack.getMappedPort(S3_PORT))
        );
    }

}