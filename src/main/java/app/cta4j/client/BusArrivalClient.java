package app.cta4j.client;

import app.cta4j.model.ArrivalResponse;
import feign.Param;
import feign.RequestLine;

public interface BusArrivalClient {
    /*
    @RequestLine("GET /ttarrivals.aspx?mapid={stationId}")
    ArrivalResponse getStationArrivals(@Param("stationId") String stationId);

    @RequestLine("GET /ttfollow.aspx?runnumber={run}")
    ArrivalResponse getTrainArrivals(@Param("run") String run);
     */
}
