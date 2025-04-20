package app.cta4j.model.bus;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;
import lombok.Value;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;

import java.math.BigDecimal;
import java.util.Objects;

@Value
@Builder
@DynamoDbImmutable(builder = Stop.StopBuilder.class)
public class Stop {
    @JsonAlias("stpid")
    String id;

    @JsonAlias("stpnm")
    String name;
}
