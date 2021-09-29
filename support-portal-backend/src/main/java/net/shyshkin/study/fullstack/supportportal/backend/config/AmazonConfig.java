package net.shyshkin.study.fullstack.supportportal.backend.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("image-s3")
public class AmazonConfig {

    @Bean
    public AmazonS3 s3() {
        return AmazonS3ClientBuilder.defaultClient();
    }
}
