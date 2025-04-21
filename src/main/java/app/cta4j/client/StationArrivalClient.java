package app.cta4j.client;

import app.cta4j.exception.ClientException;
import app.cta4j.model.ArrivalBody;
import app.cta4j.model.ArrivalResponse;
import app.cta4j.model.train.StationArrival;
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
public final class StationArrivalClient {
    private final SecretService secretService;

    private final ClientUtils clientUtils;

    private final ObjectMapper objectMapper;

    @Inject
    public StationArrivalClient(SecretService secretService, ClientUtils clientUtils, ObjectMapper objectMapper) {
        this.secretService = Objects.requireNonNull(secretService);

        this.clientUtils = Objects.requireNonNull(clientUtils);

        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    private HttpUrl getBaseUrl() throws ClientException {
        String baseUrl = this.secretService.getSecret("TRAIN_API_URL");

        String apiKey = this.secretService.getSecret("TRAIN_API_KEY");

        HttpUrl url = HttpUrl.parse(baseUrl);

        if (url == null) {
            String message = "Failed to parse the base URL: %s".formatted(baseUrl);

            throw new ClientException(message);
        }

        url = url.newBuilder()
                 .addQueryParameter("key", apiKey)
                 .addQueryParameter("outputType", "json")
                 .build();

        return url;
    }

    private List<StationArrival> extractArrivals(String body) throws ClientException {
        Objects.requireNonNull(body);

        TypeReference<ArrivalResponse<StationArrival>> typeReference = new TypeReference<>() {};

        ArrivalResponse<StationArrival> arrivalResponse;

        try {
            arrivalResponse = this.objectMapper.readValue(body, typeReference);
        } catch (IOException e) {
            String message = "Failed to parse the response body";

            throw new ClientException(message, e);
        }

        ArrivalBody<StationArrival> arrivalBody = arrivalResponse.body();

        if (arrivalBody == null) {
            String message = "Response body is null";

            throw new ClientException(message);
        }

        List<StationArrival> arrivals = arrivalBody.arrivals();

        if (arrivals == null) {
            throw new NotFoundResponse();
        }

        return List.copyOf(arrivals);
    }

    public List<StationArrival> getStationArrivals(String stationId) throws ClientException {
        HttpUrl baseUrl = this.getBaseUrl();

        HttpUrl url = baseUrl.newBuilder()
                             .addPathSegment("ttarrivals.aspx")
                             .addQueryParameter("mapid", stationId)
                             .build();

        ClientUtils.ResponseData responseData = this.clientUtils.makeGetRequest(url);

        String body = responseData.body();

        return this.extractArrivals(body);
    }

    public List<StationArrival> getTrainArrivals(String run) throws ClientException {
        Objects.requireNonNull(run);

        HttpUrl baseUrl = this.getBaseUrl();

        HttpUrl url = baseUrl.newBuilder()
                             .addPathSegment("ttfollow.aspx")
                             .addQueryParameter("runnumber", run)
                             .build();

        ClientUtils.ResponseData responseData = this.clientUtils.makeGetRequest(url);

        String body = responseData.body();

        return this.extractArrivals(body);
    }
}
