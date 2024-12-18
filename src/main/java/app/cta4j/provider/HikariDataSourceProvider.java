package app.cta4j.provider;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class HikariDataSourceProvider implements Provider<HikariDataSource> {
    private final SecretCache secretCache;

    private static final Logger LOGGER;

    static {
        LOGGER = LoggerFactory.getLogger(HikariDataSourceProvider.class);
    }

    @Inject
    public HikariDataSourceProvider(SecretCache secretCache) {
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

            HikariDataSourceProvider.LOGGER.error(message);

            throw new RuntimeException(e);
        }

        return secrets;
    }

    @Override
    public HikariDataSource get() {
        Map<String, String> secrets = this.getSecrets();

        HikariConfig config = new HikariConfig();

        String jdbcUrl = secrets.get("JDBC_URL");

        config.setJdbcUrl(jdbcUrl);

        String username = secrets.get("JDBC_USERNAME");

        config.setUsername(username);

        String password = secrets.get("JDBC_PASSWORD");

        config.setPassword(password);

        return new HikariDataSource(config);
    }
}
