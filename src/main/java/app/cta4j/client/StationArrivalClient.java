package app.cta4j.client;

import app.cta4j.model.ArrivalResponse;
import app.cta4j.model.train.StationArrival;
import feign.Param;
import feign.RequestLine;

public interface StationArrivalClient {
    @RequestLine("GET /ttarrivals.aspx?mapid={stationId}")
    ArrivalResponse<StationArrival> getStationArrivals(@Param("stationId") String stationId);

    @RequestLine("GET /ttfollow.aspx?runnumber={run}")
    ArrivalResponse<StationArrival> getTrainArrivals(@Param("run") String run);
}
