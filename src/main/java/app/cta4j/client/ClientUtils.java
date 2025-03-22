package app.cta4j.client;

import app.cta4j.exception.ClientException;
import app.cta4j.model.Arrival;
import app.cta4j.model.ArrivalBody;
import app.cta4j.model.ArrivalResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.failsafe.RetryPolicy;
import dev.failsafe.okhttp.FailsafeCall;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Singleton
public final class ClientUtils {
    private final OkHttpClient httpClient;

    private final ObjectMapper objectMapper;

    @Inject
    public ClientUtils(OkHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = Objects.requireNonNull(httpClient);

        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    public record ResponseData(int statusCode, String body) {
        public ResponseData {
            Objects.requireNonNull(body);
        }
    }

    public ResponseData makeGetRequest(HttpUrl url) throws ClientException {
        Objects.requireNonNull(url);

        Request request = new Request.Builder()
            .url(url)
            .get()
            .header("Accept", "application/json")
            .build();

        RetryPolicy<Response> retryPolicy = RetryPolicy.ofDefaults();

        Call call = this.httpClient.newCall(request);

        FailsafeCall failsafeCall = FailsafeCall.with(retryPolicy)
                                                .compose(call);

        int code;

        String body = null;

        try (Response response = failsafeCall.execute()) {
            code = response.code();

            ResponseBody responseBody = response.body();

            if (responseBody != null) {
                body = responseBody.string();
            }
        } catch (IOException e) {
            String message = "Failed to make a GET request to %s".formatted(url);

            throw new ClientException(message, e);
        }

        int expectedCode = 200;

        if (code != expectedCode) {
            String message = "Unexpected response code for URL %s: %s".formatted(url, code);

            throw new ClientException(message);
        }

        if (body == null) {
            String message = "Response body is null for URL %s".formatted(url);

            throw new ClientException(message);
        }

        return new ResponseData(code, body);
    }

    public <T extends Arrival> List<T> extractArrivals(String body) throws ClientException {
        Objects.requireNonNull(body);

        TypeReference<ArrivalResponse<T>> typeReference = new TypeReference<>() {};

        ArrivalResponse<T> arrivalResponse;

        try {
            arrivalResponse = this.objectMapper.readValue(body, typeReference);
        } catch (IOException e) {
            String message = "Failed to parse the response body";

            throw new ClientException(message, e);
        }

        ArrivalBody<T> arrivalBody = arrivalResponse.body();

        if (arrivalBody == null) {
            String message = "Response body is null";

            throw new ClientException(message);
        }

        List<T> arrivals = arrivalBody.arrivals();

        if (arrivals == null) {
            String message = "Arrivals list is null";

            throw new ClientException(message);
        }

        return arrivals;
    }
}
