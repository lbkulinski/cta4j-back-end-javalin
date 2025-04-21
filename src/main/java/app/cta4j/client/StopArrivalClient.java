package app.cta4j.client;

import app.cta4j.exception.ClientException;
import app.cta4j.model.ArrivalBody;
import app.cta4j.model.ArrivalResponse;
import app.cta4j.model.bus.StopArrival;
import app.cta4j.service.SecretService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.NotFoundResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Singleton
public final class StopArrivalClient {
    private final SecretService secretService;

    private final ClientUtils clientUtils;

    private final ObjectMapper objectMapper;

    @Inject
    public StopArrivalClient(SecretService secretService, ClientUtils clientUtils, ObjectMapper objectMapper) {
        this.secretService = Objects.requireNonNull(secretService);

        this.clientUtils = Objects.requireNonNull(clientUtils);

        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    private HttpUrl getBaseUrl() throws ClientException {
        String baseUrl = this.secretService.getSecret("BUS_API_URL");

        String apiKey = this.secretService.getSecret("BUS_API_KEY");

        HttpUrl url = HttpUrl.parse(baseUrl);

        if (url == null) {
            String message = "Failed to parse the base URL: %s".formatted(baseUrl);

            throw new ClientException(message);
        }

        url = url.newBuilder()
                 .addQueryParameter("key", apiKey)
                 .addQueryParameter("format", "json")
                 .build();

        return url;
    }

    private List<StopArrival> extractArrivals(String body) throws ClientException {
        Objects.requireNonNull(body);

        TypeReference<ArrivalResponse<StopArrival>> typeReference = new TypeReference<>() {};

        ArrivalResponse<StopArrival> arrivalResponse;

        try {
            arrivalResponse = this.objectMapper.readValue(body, typeReference);
        } catch (IOException e) {
            String message = "Failed to parse the response body";

            throw new ClientException(message, e);
        }

        ArrivalBody<StopArrival> arrivalBody = arrivalResponse.body();

        if (arrivalBody == null) {
            String message = "Response body is null";

            throw new ClientException(message);
        }

        List<StopArrival> arrivals = arrivalBody.arrivals();

        if (arrivals == null) {
            throw new NotFoundResponse();
        }

        return List.copyOf(arrivals);
    }

    public List<StopArrival> getStopArrivals(String routeId, String stopId) throws ClientException {
        Objects.requireNonNull(routeId);

        Objects.requireNonNull(stopId);

        HttpUrl baseUrl = this.getBaseUrl();

        HttpUrl url = baseUrl.newBuilder()
                             .addPathSegment("getpredictions")
                             .addQueryParameter("rt", routeId)
                             .addQueryParameter("stpid", stopId)
                             .build();

        ClientUtils.ResponseData responseData = this.clientUtils.makeGetRequest(url);

        String body = responseData.body();

        return this.extractArrivals(body);
    }

    public List<StopArrival> getBusArrivals(String id) throws ClientException {
        Objects.requireNonNull(id);

        HttpUrl baseUrl = this.getBaseUrl();

        HttpUrl url = baseUrl.newBuilder()
                             .addPathSegment("getpredictions")
                             .addQueryParameter("vid", id)
                             .build();

        ClientUtils.ResponseData responseData = this.clientUtils.makeGetRequest(url);

        String body = responseData.body();

        return this.extractArrivals(body);
    }
}
