package app.cta4j.module;

import app.cta4j.client.StopArrivalClient;
import app.cta4j.client.StationArrivalClient;
import app.cta4j.provider.*;
import com.amazonaws.secretsmanager.caching.SecretCache;
import com.google.inject.AbstractModule;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;

public final class ApplicationModule extends AbstractModule {
    @Override
    protected void configure() {
        this.bind(SecretCache.class)
            .toProvider(SecretCacheProvider.class);

        this.bind(HikariDataSource.class)
            .toProvider(HikariDataSourceProvider.class);

        this.bind(DSLContext.class)
            .toProvider(DSLContextProvider.class);

        this.bind(StationArrivalClient.class)
            .toProvider(StationArrivalClientProvider.class);

        this.bind(StopArrivalClient.class)
            .toProvider(StopArrivalClientProvider.class);
    }
}
