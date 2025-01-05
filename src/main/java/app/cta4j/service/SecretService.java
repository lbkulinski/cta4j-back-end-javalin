package app.cta4j.service;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SecretService {
    private final SecretCache secretCache;

    private static final Logger LOGGER;

    static {
        LOGGER = LoggerFactory.getLogger(SecretService.class);
    }

    @Inject
    public SecretService(SecretCache secretCache) {
        this.secretCache = Objects.requireNonNull(secretCache);
    }

    public String getSecret(String id) {
        Objects.requireNonNull(id);

        String secretId = System.getenv("SECRET_ID");

        String secretString = this.secretCache.getSecretString(secretId);

        ObjectMapper mapper = new ObjectMapper();

        TypeReference<HashMap<String, String>> typeReference = new TypeReference<>() {};

        Map<String, String> secrets;

        try {
            secrets = mapper.readValue(secretString, typeReference);
        } catch (JsonProcessingException e) {
            String message = e.getMessage();

            SecretService.LOGGER.error(message);

            throw new RuntimeException(e);
        }

        String secret = secrets.get(id);

        if (secret == null) {
            throw new RuntimeException("Secret not found");
        }

        return secret;
    }
}
