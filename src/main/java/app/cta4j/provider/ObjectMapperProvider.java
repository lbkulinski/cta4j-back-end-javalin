package app.cta4j.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;

public final class ObjectMapperProvider implements Provider<ObjectMapper> {
    @Override
    public ObjectMapper get() {
        return new ObjectMapper();
    }
}
