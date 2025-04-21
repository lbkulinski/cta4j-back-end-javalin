package app.cta4j.model.bus;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.List;

@Value
@Builder
@DynamoDbImmutable(builder = RouteDirections.RouteDirectionsBuilder.class)
public class RouteDirections {
    @Getter(onMethod_ = {@DynamoDbPartitionKey, @DynamoDbAttribute("id")})
    String id;

    @Getter(onMethod_ = {@DynamoDbAttribute("directions")})
    List<String> directions;
}
