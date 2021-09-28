package net.shyshkin.study.fullstack.supportportal.backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProfileImageService {

    byte[] retrieveProfileImage(UUID userId, String filename);

    String persistProfileImage(UUID userId, MultipartFile profileImage, String filename);

    void clearUserStorage(UUID userId);

}
