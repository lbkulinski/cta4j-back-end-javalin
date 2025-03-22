package app.cta4j.client;

import app.cta4j.exception.ClientException;
import app.cta4j.model.bus.StopArrival;
import app.cta4j.service.SecretService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import okhttp3.*;

import java.util.List;
import java.util.Objects;

@Singleton
public final class StopArrivalClient {
    private final SecretService secretService;

    private final ClientUtils clientUtils;

    @Inject
    public StopArrivalClient(SecretService secretService, ClientUtils clientUtils) {
        this.secretService = Objects.requireNonNull(secretService);

        this.clientUtils = Objects.requireNonNull(clientUtils);
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

        return this.clientUtils.extractArrivals(body);
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

        return this.clientUtils.extractArrivals(body);
    }
}
