package app.cta4j.provider;

import app.cta4j.client.StopArrivalClient;
import com.amazonaws.secretsmanager.caching.SecretCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import feign.Feign;
import feign.RequestInterceptor;
import feign.jackson.JacksonDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StopArrivalClientProvider implements Provider<StopArrivalClient> {
    private final SecretCache secretCache;

    private static final Logger LOGGER;

    static {
        LOGGER = LoggerFactory.getLogger(StopArrivalClientProvider.class);
    }

    @Inject
    public StopArrivalClientProvider(SecretCache secretCache) {
        this.secretCache = Objects.requireNonNull(secretCache);
    }

    private Map<String, String> getSecrets() {
        String secretId = System.getenv("SECRET_ID");

        String secret = this.secretCache.getSecretString(secretId);

        ObjectMapper mapper = new ObjectMapper();

        TypeReference<HashMap<String, String>> typeReference = new TypeReference<>() {};

        Map<String, String> secrets;

        try {
            secrets = mapper.readValue(secret, typeReference);
        } catch (JsonProcessingException e) {
            String message = e.getMessage();

            StopArrivalClientProvider.LOGGER.error(message);

            throw new RuntimeException(e);
        }

        return secrets;
    }

    @Override
    public StopArrivalClient get() {
        JacksonDecoder decoder = new JacksonDecoder();

        Map<String, String> secrets = this.getSecrets();

        String apiKey = secrets.get("BUS_API_KEY");

        RequestInterceptor requestInterceptor = template -> {
            template.query("key", apiKey);
            template.query("format", "json");
        };

        return Feign.builder()
                    .decoder(decoder)
                    .requestInterceptor(requestInterceptor)
                    .target(StopArrivalClient.class, "https://ctabustracker.com/bustime/api/v2");
    }
}
