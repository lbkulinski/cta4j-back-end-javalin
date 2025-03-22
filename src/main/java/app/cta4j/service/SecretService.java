package app.cta4j.service;

import app.cta4j.exception.SecretServiceException;
import com.amazonaws.secretsmanager.caching.SecretCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Singleton
public final class SecretService {
    private final Map<String, String> idToSecret;

    @Inject
    public SecretService(SecretsManagerClient secretsManagerClient, ObjectMapper objectMapper) {
        Objects.requireNonNull(secretsManagerClient);

        Objects.requireNonNull(objectMapper);

        this.idToSecret = SecretService.loadSecrets(secretsManagerClient, objectMapper);
    }

    private static Map<String, String> loadSecrets(SecretsManagerClient secretsManagerClient,
                                                   ObjectMapper objectMapper) {
        Objects.requireNonNull(secretsManagerClient);

        String secretId = System.getenv("SECRET_ID");

        GetSecretValueRequest request = GetSecretValueRequest.builder()
                                                             .secretId(secretId)
                                                             .build();

        GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);

        String secretString = response.secretString();

        TypeReference<HashMap<String, String>> typeReference = new TypeReference<>() {};

        Map<String, String> idToSecret;

        try {
            idToSecret = objectMapper.readValue(secretString, typeReference);
        } catch (JsonProcessingException e) {
            String message = "An error occurred while parsing the secret string";

            throw new SecretServiceException(message, e);
        }

        return idToSecret;
    }

    public String getSecret(String id) {
        Objects.requireNonNull(id);

        String secret = this.idToSecret.get(id);

        if (secret == null) {
            String message = "Secret with ID %s not found".formatted(id);

            throw new SecretServiceException(message);
        }

        return secret;
    }
}
