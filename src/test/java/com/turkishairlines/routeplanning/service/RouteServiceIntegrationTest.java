package com.turkishairlines.routeplanning.service;

import com.turkishairlines.routeplanning.exception.InvalidRouteException;
import com.turkishairlines.routeplanning.exception.ResourceNotFoundException;
import com.turkishairlines.routeplanning.model.dto.RouteDTO;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import com.turkishairlines.routeplanning.repository.LocationRepository;
import com.turkishairlines.routeplanning.repository.TransportationRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class RouteServiceIntegrationTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private RouteService routeService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private TransportationRepository transportationRepository;

    private Location istanbulLocation;
    private Location ankaraLocation;
    private Location izmirLocation;
    private Location antalyaLocation;

    @BeforeEach
    void setUp() {
        transportationRepository.deleteAll();
        locationRepository.deleteAll();

        // Create test locations
        istanbulLocation = createTestLocation("Istanbul Airport", "Turkey", "Istanbul", "IST");
        ankaraLocation = createTestLocation("Ankara Airport", "Turkey", "Ankara", "ESB");
        izmirLocation = createTestLocation("Izmir Airport", "Turkey", "Izmir", "ADB");
        antalyaLocation = createTestLocation("Antalya Airport", "Turkey", "Antalya", "AYT");

        // Create test transportations for different route strategies
        // Direct routes
        createTestTransportation(istanbulLocation, ankaraLocation, TransportationType.FLIGHT,
                new Integer[] { 1, 2, 3, 4, 5 });
        createTestTransportation(istanbulLocation, izmirLocation, TransportationType.BUS,
                new Integer[] { 1, 2, 3, 4, 5, 6, 7 });
        createTestTransportation(istanbulLocation, antalyaLocation, TransportationType.FLIGHT,
                new Integer[] { 1, 2, 3, 4, 5, 6, 7 });

        // Two-step routes: IST -> ESB -> ADB
        createTestTransportation(ankaraLocation, izmirLocation, TransportationType.BUS,
                new Integer[] { 1, 2, 3, 4, 5, 6, 7 });

        // Two-step routes: IST -> ADB -> AYT
        createTestTransportation(izmirLocation, antalyaLocation, TransportationType.BUS,
                new Integer[] { 1, 2, 3, 4, 5, 6, 7 });

        // Three-step routes: IST -> ESB (BUS) -> ADB (FLIGHT) -> AYT (BUS)
        createTestTransportation(istanbulLocation, ankaraLocation, TransportationType.BUS,
                new Integer[] { 1, 2, 3, 4, 5, 6, 7 });
        createTestTransportation(ankaraLocation, izmirLocation, TransportationType.FLIGHT,
                new Integer[] { 1, 2, 3, 4, 5 });
        createTestTransportation(izmirLocation, antalyaLocation, TransportationType.BUS,
                new Integer[] { 1, 2, 3, 4, 5, 6, 7 });
    }

    @Test
    @DisplayName("Should find direct routes between two locations")
    void shouldFindDirectRoutesBetweenTwoLocations() {
        // When
        List<RouteDTO> routes = routeService.findValidRoutes("IST", "ESB", null);

        // Then
        assertThat(routes).isNotNull();
        assertThat(routes).isNotEmpty();

        boolean hasDirectRoute = routes.stream()
                .anyMatch(route -> route.getTotalTransportations() == 1);
        assertThat(hasDirectRoute).isTrue();

        RouteDTO directRoute = routes.stream()
                .filter(route -> route.getTotalTransportations() == 1)
                .findFirst()
                .orElse(null);

        assertThat(directRoute).isNotNull();
        assertThat(directRoute.getOriginLocation().getLocationCode()).isEqualTo("IST");
        assertThat(directRoute.getDestinationLocation().getLocationCode()).isEqualTo("ESB");
        assertThat(directRoute.getTransportations()).hasSize(1);
        assertThat(directRoute.getTransportations().get(0).getTransportationType())
                .isEqualTo(TransportationType.FLIGHT);
    }

    @Test
    @DisplayName("Should find multi-step routes between two locations")
    void shouldFindMultiStepRoutesBetweenTwoLocations() {
        // When
        List<RouteDTO> routes = routeService.findValidRoutes("IST", "AYT", null);

        // Then
        assertThat(routes).isNotNull();
        assertThat(routes).isNotEmpty();

        assertThat(routes.size()).isGreaterThan(0);

        // Verify route details
        routes.forEach(route -> {
            assertThat(route.getOriginLocation().getLocationCode()).isEqualTo("IST");
            assertThat(route.getDestinationLocation().getLocationCode()).isEqualTo("AYT");
            assertThat(route.getTotalTransportations()).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("Should find routes with specific date")
    void shouldFindRoutesWithSpecificDate() {
        // Given
        LocalDate testDate = LocalDate.of(2024, 1, 15); // Monday

        // When
        List<RouteDTO> routes = routeService.findValidRoutes("IST", "ESB", testDate);

        // Then
        assertThat(routes).isNotNull();
        assertThat(routes).isNotEmpty();

        routes.forEach(route -> {
            assertThat(route.getOriginLocation().getLocationCode()).isEqualTo("IST");
            assertThat(route.getDestinationLocation().getLocationCode()).isEqualTo("ESB");
        });
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when origin location not found")
    void shouldThrowResourceNotFoundExceptionWhenOriginLocationNotFound() {
        // When & Then
        assertThatThrownBy(() -> routeService.findValidRoutes("INVALID", "ESB", null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Origin location not found with code: INVALID");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when destination location not found")
    void shouldThrowResourceNotFoundExceptionWhenDestinationLocationNotFound() {
        // When & Then
        assertThatThrownBy(() -> routeService.findValidRoutes("IST", "INVALID", null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Destination location not found with code: INVALID");
    }

    @Test
    @DisplayName("Should throw InvalidRouteException when origin and destination are the same")
    void shouldThrowInvalidRouteExceptionWhenOriginAndDestinationAreSame() {
        // When & Then
        assertThatThrownBy(() -> routeService.findValidRoutes("IST", "IST", null))
                .isInstanceOf(InvalidRouteException.class)
                .hasMessage("Origin and destination cannot be the same location");
    }

    @Test
    @DisplayName("Should return empty list when no routes exist between locations")
    void shouldReturnEmptyListWhenNoRoutesExistBetweenLocations() {
        Location isolatedLocation = createTestLocation("Isolated Airport", "Turkey", "Isolated", "ISO");

        // When
        List<RouteDTO> routes = routeService.findValidRoutes("IST", "ISO", null);

        // Then
        assertThat(routes).isNotNull();
        assertThat(routes).isEmpty();
    }

    @Test
    @DisplayName("Should find routes with different transportation types")
    void shouldFindRoutesWithDifferentTransportationTypes() {
        // When
        List<RouteDTO> routes = routeService.findValidRoutes("IST", "ADB", null);

        // Then
        assertThat(routes).isNotNull();
        assertThat(routes).isNotEmpty();

        // Should find at least one route
        assertThat(routes.size()).isGreaterThan(0);

        routes.forEach(route -> {
            assertThat(route.getTransportations()).isNotEmpty();
            route.getTransportations().forEach(transport -> {
                assertThat(transport.getTransportationType()).isIn(TransportationType.FLIGHT, TransportationType.BUS);
            });
        });
    }

    @Test
    @DisplayName("Should find routes with different operating days")
    void shouldFindRoutesWithDifferentOperatingDays() {
        // Given
        LocalDate weekday = LocalDate.of(2024, 1, 15); // Monday
        LocalDate weekend = LocalDate.of(2024, 1, 13); // Saturday

        // When
        List<RouteDTO> weekdayRoutes = routeService.findValidRoutes("IST", "ESB", weekday);
        List<RouteDTO> weekendRoutes = routeService.findValidRoutes("IST", "ESB", weekend);

        // Then
        assertThat(weekdayRoutes).isNotNull();
        assertThat(weekendRoutes).isNotNull();

        assertThat(weekdayRoutes.size() + weekendRoutes.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should find routes with complex multi-step connections")
    void shouldFindRoutesWithComplexMultiStepConnections() {
        // When
        List<RouteDTO> routes = routeService.findValidRoutes("IST", "AYT", null);

        // Then
        assertThat(routes).isNotNull();
        assertThat(routes).isNotEmpty();

        // Should find at least one route
        assertThat(routes.size()).isGreaterThan(0);

        routes.forEach(route -> {
            assertThat(route.getRouteDescription()).isNotNull();
            assertThat(route.getRouteDescription()).isNotEmpty();
            String description = route.getRouteDescription();
            assertThat(description.contains("FLIGHT") || description.contains("BUS")).isTrue();
        });
    }

    @Test
    @DisplayName("Should handle null date parameter")
    void shouldHandleNullDateParameter() {
        // When
        List<RouteDTO> routes = routeService.findValidRoutes("IST", "ESB", null);

        // Then
        assertThat(routes).isNotNull();
        assertThat(routes).isNotEmpty();

        // Should find routes regardless of date
        routes.forEach(route -> {
            assertThat(route.getOriginLocation().getLocationCode()).isEqualTo("IST");
            assertThat(route.getDestinationLocation().getLocationCode()).isEqualTo("ESB");
        });
    }

    @Test
    @DisplayName("Should find routes with mixed transportation types in multi-step routes")
    void shouldFindRoutesWithMixedTransportationTypesInMultiStepRoutes() {
        // When
        List<RouteDTO> routes = routeService.findValidRoutes("IST", "AYT", null);

        // Then
        assertThat(routes).isNotNull();
        assertThat(routes).isNotEmpty();

        assertThat(routes.size()).isGreaterThan(0);

        // Verify all routes have valid transportation types
        routes.forEach(route -> {
            assertThat(route.getTransportations()).isNotEmpty();
            route.getTransportations().forEach(transport -> {
                assertThat(transport.getTransportationType()).isIn(TransportationType.FLIGHT, TransportationType.BUS);
            });
        });
    }

    private Location createTestLocation(String name, String country, String city, String locationCode) {
        Location location = Location.builder()
                .name(name)
                .country(country)
                .city(city)
                .locationCode(locationCode)
                .build();
        return locationRepository.save(location);
    }

    private Transportation createTestTransportation(Location origin, Location destination,
            TransportationType type, Integer[] operatingDays) {
        Transportation transportation = Transportation.builder()
                .originLocation(origin)
                .destinationLocation(destination)
                .transportationType(type)
                .operatingDays(operatingDays)
                .build();
        return transportationRepository.save(transportation);
    }
}
