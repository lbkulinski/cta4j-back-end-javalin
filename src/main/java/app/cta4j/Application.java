package app.cta4j;

import app.cta4j.module.ApplicationModule;
import app.cta4j.service.StationService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.javalin.Javalin;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
               .get("/api/stations", ctx -> ctx.json(service.getStations()))
               .exception(Exception.class, (e, ctx) -> {
                   String message = e.getMessage();

                   Application.LOGGER.error(message, e);

                   ctx.status(500);
               })
               .start(8080);
    }
}
