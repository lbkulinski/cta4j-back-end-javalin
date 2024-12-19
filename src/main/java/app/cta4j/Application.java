package app.cta4j;

import app.cta4j.model.Arrival;
import app.cta4j.model.Station;
import app.cta4j.module.ApplicationModule;
import app.cta4j.service.StationService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public final class Application {
    private static final Logger LOGGER;

    static {
        LOGGER = LoggerFactory.getLogger(Application.class);
    }

    public static void main(String[] args) {
        ApplicationModule module = new ApplicationModule();

        Injector injector = Guice.createInjector(module);

        StationService service = injector.getInstance(StationService.class);

        Javalin.create()
               .get("/api/stations", ctx -> {
                   Set<Station> stations = service.getStations();

                   ctx.json(stations);
               })
               .get("/api/stations/{stationId}/arrivals", ctx -> {
                   String stationId = ctx.pathParam("stationId");

                   Set<Arrival> arrivals = service.getArrivals(stationId);

                   ctx.json(arrivals);
               })
               .exception(Exception.class, (e, ctx) -> {
                   String message = e.getMessage();

                   Application.LOGGER.error(message, e);

                   ctx.status(500);
               })
               .start(8080);
    }
}
