package app.cta4j;

import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.model.bus.Direction;
import app.cta4j.model.bus.Route;
import app.cta4j.model.bus.Stop;
import app.cta4j.model.bus.StopArrival;
import app.cta4j.model.train.Station;
import app.cta4j.model.train.StationArrival;
import app.cta4j.module.ApplicationModule;
import app.cta4j.service.BusService;
import app.cta4j.service.StationService;
import app.cta4j.service.StopService;
import app.cta4j.service.TrainService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public final class Application {
    private static final Logger LOGGER;

    static {
        LOGGER = LoggerFactory.getLogger(Application.class);
    }

    public static void main(String[] args) {
        ApplicationModule module = new ApplicationModule();

        Injector injector = Guice.createInjector(module);

        StationService stationService = injector.getInstance(StationService.class);

        TrainService trainService = injector.getInstance(TrainService.class);

        StopService stopService = injector.getInstance(StopService.class);

        BusService busService = injector.getInstance(BusService.class);

        Javalin.create()
               .get("/api/stations", ctx -> {
                   Set<Station> stations = stationService.getStations();

                   ctx.json(stations);
               })
               .get("/api/stations/{stationId}/arrivals", ctx -> {
                   String stationId = ctx.pathParam("stationId");

                   Set<StationArrival> arrivals = stationService.getArrivals(stationId);

                   ctx.json(arrivals);
               })
               .get("/api/trains/{run}/arrivals", ctx -> {
                   String run = ctx.pathParam("run");

                   List<StationArrival> arrivals = trainService.getArrivals(run);

                   ctx.json(arrivals);
               })
               .get("/api/routes", ctx -> {
                   Set<Route> routes = stopService.getRoutes();

                   ctx.json(routes);
               })
               .get("/api/routes/{routeId}/directions", ctx -> {
                   String routeId = ctx.pathParam("routeId");

                   Set<Direction> directions = stopService.getDirections(routeId);

                   ctx.json(directions);
               })
               .get("/api/routes/{routeId}/directions/{direction}/stops", ctx -> {
                   String routeId = ctx.pathParam("routeId");

                   String direction = ctx.pathParam("direction");

                   Set<Stop> stops = stopService.getStops(routeId, direction);

                   ctx.json(stops);
               })
               .get("/api/routes/{routeId}/stops/{stopId}/arrivals", ctx -> {
                   String routeId = ctx.pathParam("routeId");

                   String stopId = ctx.pathParam("stopId");

                   List<StopArrival> arrivals = stopService.getArrivals(routeId, stopId);

                   ctx.json(arrivals);
               })
               .get("/api/buses/{id}/arrivals", ctx -> {
                   String id = ctx.pathParam("id");

                   List<StopArrival> arrivals = busService.getArrivals(id);

                   ctx.json(arrivals);
               })
               .exception(ResourceNotFoundException.class, (e, ctx) -> {
                   String message = e.getMessage();

                   Application.LOGGER.error(message);

                   ctx.status(404);
               })
               .exception(Exception.class, (e, ctx) -> {
                   String message = e.getMessage();

                   Application.LOGGER.error(message, e);

                   ctx.status(500);
               })
               .start(8080);
    }
}
