package app.cta4j.module;

import app.cta4j.client.StopArrivalClient;
import app.cta4j.client.StationArrivalClient;
import app.cta4j.provider.*;
import com.amazonaws.secretsmanager.caching.SecretCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import redis.clients.jedis.UnifiedJedis;

public final class ApplicationModule extends AbstractModule {
    @Override
    protected void configure() {
        this.bind(ObjectMapper.class)
            .toProvider(ObjectMapperProvider.class);

        this.bind(SecretCache.class)
            .toProvider(SecretCacheProvider.class);

        this.bind(UnifiedJedis.class)
            .toProvider(RedisClientProvider.class);

        this.bind(StationArrivalClient.class)
            .toProvider(StationArrivalClientProvider.class);

        this.bind(StopArrivalClient.class)
            .toProvider(StopArrivalClientProvider.class);
    }
}
