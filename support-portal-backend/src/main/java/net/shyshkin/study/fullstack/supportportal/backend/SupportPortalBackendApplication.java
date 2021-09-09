package net.shyshkin.study.fullstack.supportportal.backend;

import net.shyshkin.study.fullstack.supportportal.backend.constant.FileConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class SupportPortalBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SupportPortalBackendApplication.class, args);
        new File(FileConstant.USER_FOLDER).mkdirs();
    }

}
