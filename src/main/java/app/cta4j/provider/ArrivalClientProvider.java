package app.cta4j.provider;

import app.cta4j.client.ArrivalClient;
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

public class ArrivalClientProvider implements Provider<ArrivalClient> {
    private final SecretCache secretCache;

    private static final Logger LOGGER;

    static {
        LOGGER = LoggerFactory.getLogger(ArrivalClientProvider.class);
    }

    @Inject
    public ArrivalClientProvider(SecretCache secretCache) {
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

            ArrivalClientProvider.LOGGER.error(message);

            throw new RuntimeException(e);
        }

        return secrets;
    }

    @Override
    public ArrivalClient get() {
        JacksonDecoder decoder = new JacksonDecoder();

        Map<String, String> secrets = this.getSecrets();

        String apiKey = secrets.get("TRAIN_API_KEY");

        RequestInterceptor requestInterceptor = template -> {
            template.query("key", apiKey);
            template.query("outputType", "json");
        };

        return Feign.builder()
                    .decoder(decoder)
                    .requestInterceptor(requestInterceptor)
                    .target(ArrivalClient.class, "https://lapi.transitchicago.com/api/1.0");
    }
}
