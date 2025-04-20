//package app.cta4j;
//
//import app.cta4j.client.StopArrivalClient;
//import app.cta4j.model.ArrivalBody;
//import app.cta4j.model.ArrivalResponse;
//import app.cta4j.model.bus.*;
//import app.cta4j.service.StopService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.github.benmanes.caffeine.cache.Cache;
//import io.javalin.http.NotFoundResponse;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import redis.clients.jedis.UnifiedJedis;
//
//import java.lang.reflect.Constructor;
//import java.lang.reflect.Field;
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.List;
//import java.util.Set;
//import java.util.function.Function;
//
//class StopServiceTests {
//    private UnifiedJedis jedis;
//
//    private ObjectMapper mapper;
//
//    private StopArrivalClient client;
//
//    private StopService service;
//
//    @BeforeEach
//    void setUp() {
//        this.jedis = Mockito.mock(UnifiedJedis.class);
//
//        this.mapper = new ObjectMapper();
//
//        this.client = Mockito.mock(StopArrivalClient.class);
//
//        this.service = new StopService(this.jedis, this.mapper, this.client);
//    }
//
//    @DisplayName("Test getRoutes returns cached routes")
//    @Test
//    void testGetRoutesCached() throws Exception {
//        Set<Route> expected = Set.of(
//            Route.builder()
//                 .id("22")
//                 .name("Clark")
//                 .build(),
//
//            Route.builder()
//                 .id("36")
//                 .name("Broadway")
//                 .build(),
//
//            Route.builder()
//                 .id("151")
//                 .name("Sheridan")
//                 .build()
//        );
//
//        @SuppressWarnings("unchecked")
//        Cache<String, Set<Route>> cache = Mockito.mock(Cache.class);
//
//        Field cacheField = StopService.class.getDeclaredField("routeCache");
//
//        cacheField.setAccessible(true);
//
//        cacheField.set(this.service, cache);
//
//        Mockito.when(cache.get(Mockito.eq("routes"), Mockito.any(Function.class)))
//               .thenReturn(expected);
//
//        Set<Route> actual = this.service.getRoutes();
//
//        Assertions.assertThat(actual)
//                  .containsExactlyInAnyOrderElementsOf(expected);
//    }
//
//    @DisplayName("Test getRoutes returns routes from database")
//    @Test
//    void testGetRoutesDatabase() {
//        Set<Route> expected = Set.of(
//            Route.builder()
//                 .id("22")
//                 .name("Clark")
//                 .build(),
//
//            Route.builder()
//                 .id("36")
//                 .name("Broadway")
//                 .build(),
//
//            Route.builder()
//                 .id("151")
//                 .name("Sheridan")
//                 .build()
//        );
//
//        String expectedJson;
//
//        try {
//            expectedJson = this.mapper.writeValueAsString(expected);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        Mockito.when(this.jedis.get("routes"))
//               .thenReturn(expectedJson);
//
//        Set<Route> actual = this.service.getRoutes();
//
//        Assertions.assertThat(actual)
//                  .containsExactlyInAnyOrderElementsOf(expected);
//    }
//
//    @DisplayName("Test getDirections returns cached directions")
//    @Test
//    void testGetDirectionsCached() throws Exception {
//        Set<Direction> expected = Set.of(
//            Direction.NORTHBOUND,
//            Direction.SOUTHBOUND
//        );
//
//        @SuppressWarnings("unchecked")
//        Cache<String, Set<Direction>> cache = Mockito.mock(Cache.class);
//
//        Field cacheField = StopService.class.getDeclaredField("directionCache");
//
//        cacheField.setAccessible(true);
//
//        cacheField.set(this.service, cache);
//
//        String routeId = "22";
//
//        Mockito.when(cache.get(Mockito.eq(routeId), Mockito.any(Function.class)))
//               .thenReturn(expected);
//
//        Set<Direction> actual = this.service.getDirections(routeId);
//
//        Assertions.assertThat(actual)
//                  .containsExactlyInAnyOrderElementsOf(expected);
//    }
//
//    @DisplayName("Test getDirections returns directions from database")
//    @Test
//    void testGetDirectionsDatabase() {
//        Set<Direction> expected = Set.of(
//            Direction.NORTHBOUND,
//            Direction.SOUTHBOUND
//        );
//
//        String routeId = "22";
//
//        String key = "route:%s:directions".formatted(routeId);
//
//        String expectedJson;
//
//        try {
//            expectedJson = this.mapper.writeValueAsString(expected);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        Mockito.when(this.jedis.get(key))
//               .thenReturn(expectedJson);
//
//        Set<Direction> actual = this.service.getDirections(routeId);
//
//        Assertions.assertThat(actual)
//                  .containsExactlyInAnyOrderElementsOf(expected);
//    }
//
//    @DisplayName("Test getStops returns cached stops")
//    @Test
//    void testGetStopsCached() throws Exception {
//        Set<Stop> expected = Set.of(
//            Stop.builder()
//                .id("1827")
//                .name("Clark & School/Aldine")
//                .latitude(BigDecimal.valueOf(41.941975))
//                .longitude(BigDecimal.valueOf(-87.652198000001))
//                .build(),
//
//            Stop.builder()
//                .id("1856")
//                .name("Clark & Chicago")
//                .latitude(BigDecimal.valueOf(41.89683))
//                .longitude(BigDecimal.valueOf(-87.631334999999))
//                .build(),
//
//            Stop.builder()
//                .id("15895")
//                .name("Clark & Harrison")
//                .latitude(BigDecimal.valueOf(41.873980999999))
//                .longitude(BigDecimal.valueOf(-87.630738))
//                .build()
//        );
//
//        @SuppressWarnings("unchecked")
//        Cache<Object, Set<Stop>> cache = Mockito.mock(Cache.class);
//
//        Field cacheField = StopService.class.getDeclaredField("stopCache");
//
//        cacheField.setAccessible(true);
//
//        cacheField.set(this.service, cache);
//
//        Class<?> clazz = StopService.class.getDeclaredClasses()[0];
//
//        Constructor<?> constructor = clazz.getDeclaredConstructor(String.class, String.class);
//
//        constructor.setAccessible(true);
//
//        String routeId = "22";
//
//        String direction = "Southbound";
//
//        Object key = constructor.newInstance(routeId, direction);
//
//        Mockito.when(cache.get(Mockito.eq(key), Mockito.any(Function.class)))
//               .thenReturn(expected);
//
//        Set<Stop> actual = this.service.getStops(routeId, direction);
//
//        Assertions.assertThat(actual)
//                  .containsExactlyInAnyOrderElementsOf(expected);
//    }
//
//    @DisplayName("Test getStops returns stops from database")
//    @Test
//    void testGetStopsDatabase() {
//        Set<Stop> expected = Set.of(
//            Stop.builder()
//                .id("1827")
//                .name("Clark & School/Aldine")
//                .latitude(BigDecimal.valueOf(41.941975))
//                .longitude(BigDecimal.valueOf(-87.652198000001))
//                .build(),
//
//            Stop.builder()
//                .id("1856")
//                .name("Clark & Chicago")
//                .latitude(BigDecimal.valueOf(41.89683))
//                .longitude(BigDecimal.valueOf(-87.631334999999))
//                .build(),
//
//            Stop.builder()
//                .id("15895")
//                .name("Clark & Harrison")
//                .latitude(BigDecimal.valueOf(41.873980999999))
//                .longitude(BigDecimal.valueOf(-87.630738))
//                .build()
//        );
//
//        String expectedJson;
//
//        try {
//            expectedJson = this.mapper.writeValueAsString(expected);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//        String routeId = "22";
//
//        String direction = "Southbound";
//
//        String key = "route:%s:direction:%s:stops".formatted(routeId, direction);
//
//        Mockito.when(this.jedis.get(key))
//               .thenReturn(expectedJson);
//
//        Set<Stop> actual = this.service.getStops(routeId, direction);
//
//        Assertions.assertThat(actual)
//                  .containsExactlyInAnyOrderElementsOf(expected);
//    }
//
//    @DisplayName("Test getArrivals returns arrivals")
//    @Test
//    void testGetArrivals() {
//        List<StopArrival> expected = List.of(
//            StopArrival.builder()
//                       .id("4168")
//                       .type(StopEventType.ARRIVAL)
//                       .stop("Clark & School/Aldine")
//                       .route("22")
//                       .destination("Harrison")
//                       .predictionTime(Instant.parse("2024-12-29T17:24:00Z"))
//                       .arrivalTime(Instant.parse("2024-12-29T17:35:00Z"))
//                       .delayed(false)
//                       .build(),
//
//            StopArrival.builder()
//                       .id("4351")
//                       .type(StopEventType.ARRIVAL)
//                       .stop("Clark & School/Aldine")
//                       .route("22")
//                       .destination("Harrison")
//                       .predictionTime(Instant.parse("2024-12-29T17:24:00Z"))
//                       .arrivalTime(Instant.parse("2024-12-29T17:35:00Z"))
//                       .delayed(false)
//                       .build(),
//
//            StopArrival.builder()
//                       .id("4399")
//                       .type(StopEventType.ARRIVAL)
//                       .stop("Clark & School/Aldine")
//                       .route("22")
//                       .destination("Harrison")
//                       .predictionTime(Instant.parse("2024-12-29T17:24:00Z"))
//                       .arrivalTime(Instant.parse("2024-12-29T17:47:00Z"))
//                       .delayed(true)
//                       .build()
//        );
//
//        ArrivalBody<StopArrival> body = new ArrivalBody<>(expected);
//
//        ArrivalResponse<StopArrival> response = new ArrivalResponse<>(body);
//
//        String routeId = "22";
//
//        String stopId = "1827";
//
//        Mockito.when(this.client.getStopArrivals(routeId, stopId))
//               .thenReturn(response);
//
//        List<StopArrival> actual = this.service.getArrivals(routeId, stopId);
//
//        Assertions.assertThat(actual)
//                  .hasSameElementsAs(expected);
//    }
//
//    @DisplayName("Test getArrivals throws runtime exception with null response")
//    @Test
//    void testGetArrivalsThrowsExceptionNullResponse() {
//        String routeId = "22";
//
//        String stopId = "1827";
//
//        Mockito.when(this.client.getStopArrivals(routeId, stopId))
//               .thenReturn(null);
//
//        Assertions.assertThatThrownBy(() -> this.service.getArrivals(routeId, stopId))
//                  .isInstanceOf(RuntimeException.class)
//                  .hasMessage("The arrival response is null for route ID %s and stop ID %s".formatted(routeId, stopId));
//    }
//
//    @DisplayName("Test getArrivals throws runtime exception with null body")
//    @Test
//    void testGetArrivalsThrowsExceptionNullBody() {
//        String routeId = "22";
//
//        String stopId = "1827";
//
//        ArrivalResponse<StopArrival> response = new ArrivalResponse<>(null);
//
//        Mockito.when(this.client.getStopArrivals(routeId, stopId))
//               .thenReturn(response);
//
//        Assertions.assertThatThrownBy(() -> this.service.getArrivals(routeId, stopId))
//                  .isInstanceOf(RuntimeException.class)
//                  .hasMessage("The arrival body is null for route ID %s and stop ID %s".formatted(routeId, stopId));
//    }
//
//    @DisplayName("Test getArrivals throws resource not found exception with null arrivals")
//    @Test
//    void testGetArrivalsNotFound() {
//        String routeId = "22";
//
//        String stopId = "1827";
//
//        ArrivalBody<StopArrival> body = new ArrivalBody<>(null);
//
//        ArrivalResponse<StopArrival> response = new ArrivalResponse<>(body);
//
//        Mockito.when(this.client.getStopArrivals(routeId, stopId))
//               .thenReturn(response);
//
//        Assertions.assertThatThrownBy(() -> this.service.getArrivals(routeId, stopId))
//                  .isInstanceOf(NotFoundResponse.class)
//                  .hasMessage("The List of arrivals is null for route ID %s and stop ID %s".formatted(routeId, stopId));
//    }
//}
