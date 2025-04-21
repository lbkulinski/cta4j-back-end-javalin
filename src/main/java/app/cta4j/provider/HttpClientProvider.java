package app.cta4j.provider;

import jakarta.inject.Provider;
import okhttp3.OkHttpClient;

public final class HttpClientProvider implements Provider<OkHttpClient> {
    @Override
    public OkHttpClient get() {
        return new OkHttpClient();
    }
}
