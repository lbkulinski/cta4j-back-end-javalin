package app.cta4j.module;

import app.cta4j.client.ArrivalClient;
import app.cta4j.provider.ArrivalClientProvider;
import app.cta4j.provider.DSLContextProvider;
import app.cta4j.provider.HikariDataSourceProvider;
import app.cta4j.provider.SecretCacheProvider;
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

        this.bind(ArrivalClient.class)
            .toProvider(ArrivalClientProvider.class);
    }
}
