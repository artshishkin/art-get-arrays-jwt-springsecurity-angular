package net.shyshkin.study.fullstack.supportportal.backend.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("image-s3-localstack")
public class LocalStackAmazonConfig {

    @Value("${config.aws.region}")          private String region;
    @Value("${config.aws.s3.url}")          private String s3EndpointUrl;
    @Value("${config.aws.s3.bucket-name}")  private String bucketName;
    @Value("${config.aws.s3.access-key}")   private String accessKey;
    @Value("${config.aws.s3.secret-key}")   private String secretKey;

    @Bean
    public AmazonS3 s3() {

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(getCredentialsProvider())
                .withEndpointConfiguration(getEndpointConfiguration(s3EndpointUrl))
                .build();
    }

    private AwsClientBuilder.EndpointConfiguration getEndpointConfiguration(String url) {
        return new AwsClientBuilder.EndpointConfiguration(url, region);
    }

    private AWSStaticCredentialsProvider getCredentialsProvider() {
        return new AWSStaticCredentialsProvider(getBasicAWSCredentials());
    }

    private BasicAWSCredentials getBasicAWSCredentials() {
        return new BasicAWSCredentials(accessKey, secretKey);
    }
}