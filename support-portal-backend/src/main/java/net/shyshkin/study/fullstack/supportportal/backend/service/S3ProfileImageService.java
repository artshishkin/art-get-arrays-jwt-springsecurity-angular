package net.shyshkin.study.fullstack.supportportal.backend.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shyshkin.study.fullstack.supportportal.backend.exception.domain.ImageStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@Profile("image-s3")
@RequiredArgsConstructor
public class S3ProfileImageService implements ProfileImageService {

    private final AmazonS3 amazonS3;

    @Value("${app.amazon-s3.bucket-name}")
    private String bucketName;

    @Override
    public byte[] retrieveProfileImage(UUID userId, String filename) {

        String fileKey = createFileKey(userId, filename);

        try {
            S3Object object = amazonS3.getObject(bucketName, fileKey);
            S3ObjectInputStream objectContent = object.getObjectContent();
            return IOUtils.toByteArray(objectContent);
        } catch (AmazonServiceException | IOException exception) {
            throw new ImageStorageException("Failed to download the file from Amazon S3", exception);
        }
    }

    @Override
    public String persistProfileImage(UUID userId, MultipartFile profileImage, String filename) {

//        String fileName = String.format("%s", profileImage.getOriginalFilename());
        String fileKey = createFileKey(userId, filename);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.addUserMetadata("Content-Type", profileImage.getContentType());
        objectMetadata.addUserMetadata("Content-Length", String.valueOf(profileImage.getSize()));

        try {
            amazonS3.putObject(bucketName, fileKey, profileImage.getInputStream(), objectMetadata);
        } catch (IOException | SdkClientException exception) {
            throw new ImageStorageException("Failed to persist to Amazon S3", exception);
        }

        return null;
    }

    @Override
    public void clearUserStorage(UUID userId) {

        try {
            String prefix = userId + "/";
            ObjectListing objectListing = amazonS3.listObjects(bucketName, prefix);

            objectListing.getObjectSummaries()
                    .forEach(file -> amazonS3.deleteObject(bucketName, file.getKey()));
        } catch (SdkClientException exception) {
            throw new ImageStorageException("Failed to delete objects from Amazon S3", exception);
        }
    }

    private String createFileKey(UUID userId, String filename) {
        return String.format("%s/%s", userId, filename);
    }
}
