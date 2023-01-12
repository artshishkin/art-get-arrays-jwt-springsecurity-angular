package net.shyshkin.study.fullstack.supportportal.backend.service;

import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.ImageStorageException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static net.shyshkin.study.fullstack.supportportal.backend.constant.FileConstant.*;

@Slf4j
@Service
@Profile("!image-s3 && !image-s3-localstack")
public class FileSystemProfileImageService implements ProfileImageService {

    @Override
    public byte[] retrieveProfileImage(UUID userId, String filename) {

        Path userProfileImagePath = Paths
                .get(USER_FOLDER, userId.toString(), filename);
        try {
            return Files.readAllBytes(userProfileImagePath);
        } catch (IOException exception) {
            throw new ImageStorageException("Can not retrieve image for user " + userId + " from file " + filename, exception);
        }
    }

    @Override
    public String persistProfileImage(UUID userId, MultipartFile profileImage, String filename) {
        Path userFolder = Paths.get(USER_FOLDER, userId.toString());
        try {
            if (Files.notExists(userFolder)) {
                Files.createDirectories(userFolder);
                log.debug(DIRECTORY_CREATED);
            }
            profileImage.transferTo(userFolder.resolve(USER_IMAGE_FILENAME));
            log.debug(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());

        } catch (IOException exception) {
            throw new ImageStorageException("Can not persist image for user " + userId + " from file " + profileImage, exception);
        }
        return null;
    }

    @Override
    public void clearUserStorage(UUID userId) {
        Path userFolder = Paths.get(USER_FOLDER, userId.toString());
        try {
            FileSystemUtils.deleteRecursively(userFolder);
        } catch (IOException exception) {
            throw new ImageStorageException("Can not delete folder for user " + userId, exception);
        }
    }
}
