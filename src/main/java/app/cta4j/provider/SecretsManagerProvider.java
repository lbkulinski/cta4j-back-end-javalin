package app.cta4j.provider;

import com.google.inject.Provider;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

public final class SecretsManagerProvider implements Provider<SecretsManagerClient> {
    @Override
    public SecretsManagerClient get() {
        return SecretsManagerClient.create();
    }
}
