package app.cta4j;

import app.cta4j.client.StopArrivalClient;
import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.jooq.Tables;
import app.cta4j.jooq.tables.records.RouteRecord;
import app.cta4j.model.ArrivalBody;
import app.cta4j.model.ArrivalResponse;
import app.cta4j.model.bus.*;
import app.cta4j.service.StopService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.assertj.core.api.Assertions;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

class StopServiceTests {
    private DSLContext context;

    private StopArrivalClient client;

    private StopService service;

    @BeforeEach
    void setUp() {
        this.context = Mockito.mock(DSLContext.class);

        this.client = Mockito.mock(StopArrivalClient.class);

        this.service = new StopService(this.context, this.client);
    }

    @DisplayName("Test getRoutes returns cached routes")
    @Test
    void testGetRoutesCached() throws Exception {
        Set<Route> expected = Set.of(
            Route.builder()
                 .id("22")
                 .name("Clark")
                 .build(),

            Route.builder()
                 .id("36")
                 .name("Broadway")
                 .build(),

            Route.builder()
                 .id("151")
                 .name("Sheridan")
                 .build()
        );

        @SuppressWarnings("unchecked")
        LoadingCache<String, Set<Route>> cache = Mockito.mock(LoadingCache.class);

        Field cacheField = StopService.class.getDeclaredField("routeCache");

        cacheField.setAccessible(true);

        cacheField.set(this.service, cache);

        Mockito.when(cache.get("routes"))
               .thenReturn(expected);

        Set<Route> actual = this.service.getRoutes();

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("Test getRoutes returns routes from database")
    @Test
    void testGetRoutesDatabase() {
        Set<Route> expected = Set.of(
            Route.builder()
                 .id("22")
                 .name("Clark")
                 .build(),

            Route.builder()
                 .id("36")
                 .name("Broadway")
                 .build(),

            Route.builder()
                 .id("151")
                 .name("Sheridan")
                 .build()
        );

        @SuppressWarnings("unchecked")
        SelectWhereStep<RouteRecord> selectWhereStep = Mockito.mock(SelectWhereStep.class);

        Mockito.when(this.context.selectFrom(Tables.ROUTE))
               .thenReturn(selectWhereStep);

        Mockito.when(selectWhereStep.fetchInto(Route.class))
               .thenReturn(List.copyOf(expected));

        Set<Route> actual = this.service.getRoutes();

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("Test getDirections returns cached directions")
    @Test
    void testGetDirectionsCached() throws Exception {
        Set<Direction> expected = Set.of(
            Direction.NORTHBOUND,
            Direction.SOUTHBOUND
        );

        @SuppressWarnings("unchecked")
        Cache<String, Set<Direction>> cache = Mockito.mock(Cache.class);

        Field cacheField = StopService.class.getDeclaredField("directionCache");

        cacheField.setAccessible(true);

        cacheField.set(this.service, cache);

        String routeId = "22";

        Mockito.when(cache.get(Mockito.eq(routeId), Mockito.any(Function.class)))
               .thenReturn(expected);

        Set<Direction> actual = this.service.getDirections(routeId);

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("Test getDirections returns directions from database")
    @Test
    void testGetDirectionsDatabase() {
        Set<Direction> expected = Set.of(
            Direction.NORTHBOUND,
            Direction.SOUTHBOUND
        );

        @SuppressWarnings("unchecked")
        SelectSelectStep<Record1<String>> selectSelectStep = Mockito.mock(SelectSelectStep.class);

        Mockito.when(this.context.select(DSL.upper(Tables.DIRECTION.NAME)))
               .thenReturn(selectSelectStep);

        @SuppressWarnings("unchecked")
        SelectJoinStep<Record1<String>> selectJoinStep = Mockito.mock(SelectJoinStep.class);

        Mockito.when(selectSelectStep.from(Tables.DIRECTION))
               .thenReturn(selectJoinStep);

        @SuppressWarnings("unchecked")
        SelectOnStep<Record1<String>> selectOnStep = Mockito.mock(SelectOnStep.class);

        Mockito.when(selectJoinStep.join(Tables.ROUTE_DIRECTION))
               .thenReturn(selectOnStep);

        @SuppressWarnings("unchecked")
        SelectOnConditionStep<Record1<String>> selectOnConditionStep = Mockito.mock(SelectOnConditionStep.class);

        Mockito.when(selectOnStep.on(Tables.DIRECTION.ID.eq(Tables.ROUTE_DIRECTION.DIRECTION_ID)))
               .thenReturn(selectOnConditionStep);

        @SuppressWarnings("unchecked")
        SelectConditionStep<Record1<String>> selectConditionStep = Mockito.mock(SelectConditionStep.class);

        Mockito.when(selectOnConditionStep.where(Tables.ROUTE_DIRECTION.ROUTE_ID.eq("22")))
               .thenReturn(selectConditionStep);

        Mockito.when(selectConditionStep.fetchInto(Direction.class))
               .thenReturn(List.copyOf(expected));

        String routeId = "22";

        Set<Direction> actual = this.service.getDirections(routeId);

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("Test getStops returns cached stops")
    @Test
    void testGetStopsCached() throws Exception {
        Set<Stop> expected = Set.of(
            Stop.builder()
                .id("1827")
                .name("Clark & School/Aldine")
                .build(),

            Stop.builder()
                .id("1856")
                .name("Clark & Chicago")
                .build(),

            Stop.builder()
                .id("15895")
                .name("Clark & Harrison")
                .build()
        );

        @SuppressWarnings("unchecked")
        Cache<Object, Set<Stop>> cache = Mockito.mock(Cache.class);

        Field cacheField = StopService.class.getDeclaredField("stopCache");

        cacheField.setAccessible(true);

        cacheField.set(this.service, cache);

        String routeId = "22";

        String direction = "Southbound";

        Class<?> clazz = StopService.class.getDeclaredClasses()[0];

        Constructor<?> constructor = clazz.getDeclaredConstructor(String.class, String.class);

        constructor.setAccessible(true);

        Object key = constructor.newInstance(routeId, direction);

        Mockito.when(cache.get(Mockito.eq(key), Mockito.any(Function.class)))
               .thenReturn(expected);

        Set<Stop> actual = this.service.getStops(routeId, direction);

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("Test getStops returns stops from database")
    @Test
    void testGetStopsDatabase() {
        Set<Stop> expected = Set.of(
            Stop.builder()
                .id("1827")
                .name("Clark & School/Aldine")
                .build(),

            Stop.builder()
                .id("1856")
                .name("Clark & Chicago")
                .build(),

            Stop.builder()
                .id("15895")
                .name("Clark & Harrison")
                .build()
        );

        @SuppressWarnings("unchecked")
        SelectSelectStep<Record2<Integer, String>> selectSelectStep = Mockito.mock(SelectSelectStep.class);

        Mockito.when(this.context.select(Tables.STOP.ID, Tables.STOP.NAME))
               .thenReturn(selectSelectStep);

        @SuppressWarnings("unchecked")
        SelectJoinStep<Record2<Integer, String>> selectJoinStep = Mockito.mock(SelectJoinStep.class);

        Mockito.when(selectSelectStep.from(Tables.STOP))
               .thenReturn(selectJoinStep);

        @SuppressWarnings("unchecked")
        SelectOnStep<Record2<Integer, String>> selectOnStep0 = Mockito.mock(SelectOnStep.class);

        Mockito.when(selectJoinStep.join(Tables.ROUTE_STOP))
               .thenReturn(selectOnStep0);

        @SuppressWarnings("unchecked")
        SelectOnConditionStep<Record2<Integer, String>> selectOnConditionStep0 = Mockito.mock(SelectOnConditionStep.class);

        Mockito.when(selectOnStep0.on(Tables.STOP.ID.eq(Tables.ROUTE_STOP.STOP_ID)))
               .thenReturn(selectOnConditionStep0);

        @SuppressWarnings("unchecked")
        SelectOnStep<Record2<Integer, String>> selectOnStep1 = Mockito.mock(SelectOnStep.class);

        Mockito.when(selectOnConditionStep0.join(Tables.DIRECTION))
               .thenReturn(selectOnStep1);

        @SuppressWarnings("unchecked")
        SelectOnConditionStep<Record2<Integer, String>> selectOnConditionStep1 = Mockito.mock(SelectOnConditionStep.class);

        Mockito.when(selectOnStep1.on(Tables.ROUTE_STOP.DIRECTION_ID.eq(Tables.DIRECTION.ID)))
               .thenReturn(selectOnConditionStep1);

        @SuppressWarnings("unchecked")
        SelectConditionStep<Record2<Integer, String>> routeIdSelectConditionStep = Mockito.mock(SelectConditionStep.class);

        String routeId = "22";

        Mockito.when(selectOnConditionStep1.where(Tables.ROUTE_STOP.ROUTE_ID.eq(routeId)))
               .thenReturn(routeIdSelectConditionStep);

        @SuppressWarnings("unchecked")
        SelectConditionStep<Record2<Integer, String>> directionSelectConditionStep = Mockito.mock(SelectConditionStep.class);

        String direction = "Southbound";

        Mockito.when(routeIdSelectConditionStep.and(Tables.DIRECTION.NAME.eq(direction)))
               .thenReturn(directionSelectConditionStep);

        Mockito.when(directionSelectConditionStep.fetchInto(Stop.class))
               .thenReturn(List.copyOf(expected));

        Set<Stop> actual = this.service.getStops(routeId, direction);

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("Test getArrivals returns arrivals")
    @Test
    void testGetArrivals() {
        List<StopArrival> expected = List.of(
            StopArrival.builder()
                       .id("4168")
                       .type(StopEventType.ARRIVAL)
                       .stop("Clark & School/Aldine")
                       .route("22")
                       .destination("Harrison")
                       .predictionTime(Instant.parse("2024-12-29T17:24:00Z"))
                       .arrivalTime(Instant.parse("2024-12-29T17:35:00Z"))
                       .delayed(false)
                       .build(),

            StopArrival.builder()
                       .id("4351")
                       .type(StopEventType.ARRIVAL)
                       .stop("Clark & School/Aldine")
                       .route("22")
                       .destination("Harrison")
                       .predictionTime(Instant.parse("2024-12-29T17:24:00Z"))
                       .arrivalTime(Instant.parse("2024-12-29T17:35:00Z"))
                       .delayed(false)
                       .build(),

            StopArrival.builder()
                       .id("4399")
                       .type(StopEventType.ARRIVAL)
                       .stop("Clark & School/Aldine")
                       .route("22")
                       .destination("Harrison")
                       .predictionTime(Instant.parse("2024-12-29T17:24:00Z"))
                       .arrivalTime(Instant.parse("2024-12-29T17:47:00Z"))
                       .delayed(true)
                       .build()
        );

        ArrivalBody<StopArrival> body = new ArrivalBody<>(expected);

        ArrivalResponse<StopArrival> response = new ArrivalResponse<>(body);

        String routeId = "22";

        String stopId = "1827";

        Mockito.when(this.client.getStopArrivals(routeId, stopId))
               .thenReturn(response);

        List<StopArrival> actual = this.service.getArrivals(routeId, stopId);

        Assertions.assertThat(actual)
                  .hasSameElementsAs(expected);
    }

    @DisplayName("Test getArrivals throws runtime exception with null response")
    @Test
    void testGetArrivalsThrowsExceptionNullResponse() {
        String routeId = "22";

        String stopId = "1827";

        Mockito.when(this.client.getStopArrivals(routeId, stopId))
               .thenReturn(null);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals(routeId, stopId))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival response is null for route ID %s and stop ID %s".formatted(routeId, stopId));
    }

    @DisplayName("Test getArrivals throws runtime exception with null body")
    @Test
    void testGetArrivalsThrowsExceptionNullBody() {
        String routeId = "22";

        String stopId = "1827";

        ArrivalResponse<StopArrival> response = new ArrivalResponse<>(null);

        Mockito.when(this.client.getStopArrivals(routeId, stopId))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals(routeId, stopId))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival body is null for route ID %s and stop ID %s".formatted(routeId, stopId));
    }

    @DisplayName("Test getArrivals throws resource not found exception with null arrivals")
    @Test
    void testGetArrivalsNotFound() {
        String routeId = "22";

        String stopId = "1827";

        ArrivalBody<StopArrival> body = new ArrivalBody<>(null);

        ArrivalResponse<StopArrival> response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getStopArrivals(routeId, stopId))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals(routeId, stopId))
                  .isInstanceOf(ResourceNotFoundException.class)
                  .hasMessage("The List of arrivals is null for route ID %s and stop ID %s".formatted(routeId, stopId));
    }
}
