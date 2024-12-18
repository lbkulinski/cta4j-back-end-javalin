package app.cta4j;

import app.cta4j.module.ApplicationModule;
import app.cta4j.service.StationService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.javalin.Javalin;

public final class Application {
    public static void main(String[] args) {
        ApplicationModule module = new ApplicationModule();

        Injector injector = Guice.createInjector(module);

        StationService service = injector.getInstance(StationService.class);

        Javalin.create()
               .get("/api/stations", ctx -> ctx.json(service.getStations()))
               .start(8080);
    }
}
