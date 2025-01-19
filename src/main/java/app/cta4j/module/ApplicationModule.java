package app.cta4j.module;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.secretsmanager.caching.SecretCacheConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import redis.clients.jedis.UnifiedJedis;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Module
public final class ApplicationModule {
    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

    @Provides
    @Singleton
    public SecretCache provideSecretCache() {
        SecretsManagerClient client = SecretsManagerClient.builder()
                                                          .build();

        SecretCacheConfiguration configuration = new SecretCacheConfiguration();

        configuration.setClient(client);

        return new SecretCache(configuration);
    }

    @Provides
    @Singleton
    public UnifiedJedis provideUnifiedJedis() {
        return null;
    }

//        this.bind(UnifiedJedis.class)
//            .toProvider(RedisClientProvider.class);
//
//        this.bind(StationArrivalClient.class)
//            .toProvider(StationArrivalClientProvider.class);
//
//        this.bind(StopArrivalClient.class)
//            .toProvider(StopArrivalClientProvider.class);
//    }
}
