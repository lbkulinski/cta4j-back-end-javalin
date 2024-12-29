package app.cta4j.client;

import app.cta4j.model.ArrivalResponse;
import app.cta4j.model.train.TrainArrival;
import feign.Param;
import feign.RequestLine;

public interface TrainArrivalClient {
    @RequestLine("GET /ttarrivals.aspx?mapid={stationId}")
    ArrivalResponse<TrainArrival> getStationArrivals(@Param("stationId") String stationId);

    @RequestLine("GET /ttfollow.aspx?runnumber={run}")
    ArrivalResponse<TrainArrival> getTrainArrivals(@Param("run") String run);
}
