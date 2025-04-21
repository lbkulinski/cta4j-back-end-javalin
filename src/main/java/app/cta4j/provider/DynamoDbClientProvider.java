package app.cta4j.provider;

import com.google.inject.Provider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

public final class DynamoDbClientProvider implements Provider<DynamoDbEnhancedClient> {
    @Override
    public DynamoDbEnhancedClient get() {
        return DynamoDbEnhancedClient.create();
    }
}
