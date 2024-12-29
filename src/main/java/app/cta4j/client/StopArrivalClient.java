package app.cta4j.client;

import app.cta4j.model.ArrivalResponse;
import app.cta4j.model.bus.StopArrival;
import feign.Param;
import feign.RequestLine;

public interface StopArrivalClient {
    @RequestLine("GET /getpredictions?rt={routeId}&stpid={stopId}")
    ArrivalResponse<StopArrival> getStopArrivals(@Param("routeId") String routeId , @Param("stopId") String stopId);
}
