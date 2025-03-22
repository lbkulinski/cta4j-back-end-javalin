package app.cta4j.module;

import app.cta4j.provider.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import okhttp3.OkHttpClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

public final class ApplicationModule extends AbstractModule {
    @Override
    protected void configure() {
        this.bind(ObjectMapper.class)
            .toProvider(ObjectMapperProvider.class)
            .asEagerSingleton();

        this.bind(SecretsManagerClient.class)
            .toProvider(SecretsManagerProvider.class)
            .asEagerSingleton();

        this.bind(OkHttpClient.class)
            .toProvider(HttpClientProvider.class)
            .asEagerSingleton();
    }
}
