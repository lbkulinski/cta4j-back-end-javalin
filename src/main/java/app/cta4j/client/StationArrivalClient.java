package app.cta4j.client;

import app.cta4j.exception.ClientException;
import app.cta4j.model.train.StationArrival;
import app.cta4j.service.SecretService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import okhttp3.*;

import java.util.List;
import java.util.Objects;

@Singleton
public final class StationArrivalClient {
    private final SecretService secretService;

    private final ClientUtils clientUtils;

    @Inject
    public StationArrivalClient(SecretService secretService, ClientUtils clientUtils) {
        this.secretService = Objects.requireNonNull(secretService);

        this.clientUtils = Objects.requireNonNull(clientUtils);
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

    public List<StationArrival> getStationArrivals(String stationId) throws ClientException {
        HttpUrl baseUrl = this.getBaseUrl();

        HttpUrl url = baseUrl.newBuilder()
                             .addPathSegment("ttarrivals.aspx")
                             .addQueryParameter("mapid", stationId)
                             .build();

        ClientUtils.ResponseData responseData = this.clientUtils.makeGetRequest(url);

        String body = responseData.body();

        return this.clientUtils.extractArrivals(body);
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

        return this.clientUtils.extractArrivals(body);
    }
}
