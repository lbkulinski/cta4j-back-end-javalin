package app.cta4j.client;

import app.cta4j.model.ArrivalResponse;
import feign.Param;
import feign.RequestLine;

public interface ArrivalClient {
    @RequestLine("GET /ttarrivals.aspx?mapid={stationId}")
    ArrivalResponse getArrivals(@Param("stationId") String stationId);
}
