package app.cta4j;

import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.model.Arrival;
import app.cta4j.model.Route;
import app.cta4j.model.Station;
import app.cta4j.module.ApplicationModule;
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

        Javalin.create()
               .get("/api/stations", ctx -> {
                   Set<Station> stations = stationService.getStations();

                   ctx.json(stations);
               })
               .get("/api/stations/{stationId}/arrivals", ctx -> {
                   String stationId = ctx.pathParam("stationId");

                   Set<Arrival> arrivals = stationService.getArrivals(stationId);

                   ctx.json(arrivals);
               })
               .get("/api/trains/{run}/arrivals", ctx -> {
                   String run = ctx.pathParam("run");

                   List<Arrival> arrivals = trainService.getArrivals(run);

                   ctx.json(arrivals);
               })
               .get("/api/routes", ctx -> {
                   Set<Route> routes = stopService.getRoutes();

                   ctx.json(routes);
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
