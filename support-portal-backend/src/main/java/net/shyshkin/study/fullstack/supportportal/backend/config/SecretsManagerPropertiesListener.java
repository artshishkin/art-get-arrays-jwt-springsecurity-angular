package net.shyshkin.study.fullstack.supportportal.backend.config;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

@Slf4j
public class SecretsManagerPropertiesListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {

        System.out.println("ApplicationListener<ApplicationEnvironmentPreparedEvent> invoked");
        log.info("ApplicationListener<ApplicationEnvironmentPreparedEvent> invoked");

        ConfigurableEnvironment environment = event.getEnvironment();
        String activeProfiles = environment.getProperty("spring.profiles.active");
//        if (activeProfiles == null || !activeProfiles.contains("aws-rds")) return;

        String secretJson = getSecret();

        log.debug("Retrieved secretJson from Secret Manager: {}", secretJson);
        System.out.println("Retrieved secretJson from Secret Manager: " + secretJson);

        String jasyptPassword = getString(secretJson, "jasypt_password");
//        String jwtSecret = getString(secretJson, "app_jwt_secret");
//        String springDatasourceUsername = getString(secretJson, "spring_datasource_username");
//        String springDatasourcePassword = getString(secretJson, "spring_datasource_password");

        Properties props = new Properties();
        System.setProperty("JASYPT_PASSWORD", jasyptPassword);
        props.put("jasypt.encryptor.password", jasyptPassword);

//        props.put("app.jwt.secret", jwtSecret);
//        props.put("spring.datasource.username", springDatasourceUsername);
//        props.put("spring.datasource.password", springDatasourcePassword);

        environment.getPropertySources().addFirst(new PropertiesPropertySource("aws.secret.manager", props));

    }

// Use this code snippet in your app.
// If you need more information about configurations or implementing the sample code, visit the AWS docs:
// https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-samples.html#prerequisites

    private String getSecret() {

        String secretName = "/support-portal";
        String region = "eu-north-1";

        // Create a Secrets Manager client
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
                .withRegion(region)
                .build();

        // In this sample we only handle the specific exceptions for the 'GetSecretValue' API.
        // See https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
        // We rethrow the exception by default.

        String secret = null, decodedBinarySecret = null;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (DecryptionFailureException e) {
            // Secrets Manager can't decrypt the protected secret text using the provided KMS key.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InternalServiceErrorException e) {
            // An error occurred on the server side.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InvalidParameterException e) {
            // You provided an invalid value for a parameter.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (InvalidRequestException e) {
            // You provided a parameter value that is not valid for the current state of the resource.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        } catch (ResourceNotFoundException e) {
            // We can't find the resource that you asked for.
            // Deal with the exception here, and/or rethrow at your discretion.
            throw e;
        }

        // Decrypts secret using the associated KMS CMK.
        // Depending on whether the secret is a string or binary, one of these fields will be populated.
        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
        } else {
            decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
        }

        // Your code goes here.
        return secret != null ? secret : decodedBinarySecret;
    }

    private String getString(String json, String path) {
        try {
            JsonNode root = mapper.readTree(json);
            return root.path(path).asText();
        } catch (IOException e) {
            log.error("Can't get {} from json {}", path, json, e);
            return null;
        }
    }
}
