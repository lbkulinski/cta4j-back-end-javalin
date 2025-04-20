package app.cta4j.model.bus;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.util.List;

@Value
@Builder
@DynamoDbImmutable(builder = RouteStops.RouteStopsBuilder.class)
public class RouteStops {
    @Getter(onMethod_ = {@DynamoDbPartitionKey, @DynamoDbAttribute("id")})
    String id;

    @Getter(onMethod_ = {@DynamoDbSortKey, @DynamoDbAttribute("direction")})
    String direction;

    @Getter(onMethod_ = {@DynamoDbAttribute("stops")})
    List<Stop> stops;
}
