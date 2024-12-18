package app.cta4j.service;

import app.cta4j.model.Station;
import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

public final class StationService {
    private final DSLContext context;

    private static Logger LOGGER;

    static {
        LOGGER = LoggerFactory.getLogger(StationService.class);
    }

    @Inject
    public StationService(DSLContext context) {
        this.context = Objects.requireNonNull(context);
    }

    public Set<Station> getStations() {
        return Set.of();
    }
}
