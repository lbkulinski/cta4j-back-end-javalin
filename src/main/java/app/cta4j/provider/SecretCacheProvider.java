package app.cta4j.provider;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.secretsmanager.caching.SecretCacheConfiguration;
import com.google.inject.Provider;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

public final class SecretCacheProvider implements Provider<SecretCache> {
    @Override
    public SecretCache get() {
        SecretsManagerClient client = SecretsManagerClient.builder()
                                                          .build();

        SecretCacheConfiguration configuration = new SecretCacheConfiguration();

        configuration.setClient(client);

        return new SecretCache(configuration);
    }
}
