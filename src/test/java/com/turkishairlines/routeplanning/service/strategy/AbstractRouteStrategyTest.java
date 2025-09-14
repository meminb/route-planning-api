package com.turkishairlines.routeplanning.service.strategy;

import com.turkishairlines.routeplanning.model.dto.RouteDTO;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import com.turkishairlines.routeplanning.repository.TransportationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AbstractRouteStrategyTest {

    @Mock
    private TransportationRepository transportationRepository;

    private TestableRouteStrategy testableRouteStrategy;

    private Location istanbulAirport;
    private Location londonHeathrow;

    @BeforeEach
    void setUp() {
        testableRouteStrategy = new TestableRouteStrategy(transportationRepository);

        istanbulAirport = Location.builder()
                .name("Istanbul Airport")
                .city("Istanbul")
                .country("Turkey")
                .locationCode("IST")
                .build();
        istanbulAirport.setId(1L);

        londonHeathrow = Location.builder()
                .name("London Heathrow Airport")
                .city("London")
                .country("UK")
                .locationCode("LHR")
                .build();
        londonHeathrow.setId(2L);
    }

    @Test
    void isValidRoute_WithSingleFlight_ShouldReturnTrue() {
        Transportation flight = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .build();
        flight.setId(1L);

        boolean result = testableRouteStrategy.testIsValidRoute(List.of(flight));

        assertTrue(result);
    }

    @Test
    void isValidRoute_WithTransferThenFlight_ShouldReturnTrue() {
        Transportation bus = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.BUS)
                .build();
        bus.setId(1L);

        Transportation flight = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .build();
        flight.setId(2L);

        boolean result = testableRouteStrategy.testIsValidRoute(List.of(bus, flight));

        assertTrue(result);
    }

    @Test
    void isValidRoute_WithFlightThenTransfer_ShouldReturnTrue() {
        Transportation flight = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .build();
        flight.setId(1L);

        Transportation uber = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.UBER)
                .build();
        uber.setId(2L);

        boolean result = testableRouteStrategy.testIsValidRoute(List.of(flight, uber));

        assertTrue(result);
    }

    @Test
    void isValidRoute_WithTransferFlightTransfer_ShouldReturnTrue() {
        Transportation bus = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.BUS)
                .build();
        bus.setId(1L);

        Transportation flight = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .build();
        flight.setId(2L);

        Transportation uber = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.UBER)
                .build();
        uber.setId(3L);

        boolean result = testableRouteStrategy.testIsValidRoute(List.of(bus, flight, uber));

        assertTrue(result);
    }

    @Test
    void isValidRoute_WithNoFlight_ShouldReturnFalse() {
        Transportation bus = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.BUS)
                .build();
        bus.setId(1L);

        Transportation uber = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.UBER)
                .build();
        uber.setId(2L);

        boolean result = testableRouteStrategy.testIsValidRoute(List.of(bus, uber));

        assertFalse(result);
    }

    @Test
    void isValidRoute_WithMultipleFlights_ShouldReturnFalse() {
        Transportation flight1 = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .build();
        flight1.setId(1L);

        Transportation flight2 = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .build();
        flight2.setId(2L);

        boolean result = testableRouteStrategy.testIsValidRoute(List.of(flight1, flight2));

        assertFalse(result);
    }

    @Test
    void isValidRoute_WithEmptyList_ShouldReturnFalse() {
        boolean result = testableRouteStrategy.testIsValidRoute(List.of());

        assertFalse(result);
    }

    @Test
    void isValidRoute_WithMoreThanThreeTransportations_ShouldReturnFalse() {
        Transportation bus = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.BUS)
                .build();
        bus.setId(1L);

        Transportation flight = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .build();
        flight.setId(2L);

        Transportation uber = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.UBER)
                .build();
        uber.setId(3L);

        Transportation subway = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.SUBWAY)
                .build();
        subway.setId(4L);

        boolean result = testableRouteStrategy.testIsValidRoute(List.of(bus, flight, uber, subway));

        assertFalse(result);
    }

    @Test
    void isValidRoute_WithThreeTransportationsButFlightNotInMiddle_ShouldReturnFalse() {
        Transportation bus = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.BUS)
                .build();
        bus.setId(1L);

        Transportation uber = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.UBER)
                .build();
        uber.setId(2L);

        Transportation flight = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .build();
        flight.setId(3L);

        boolean result = testableRouteStrategy.testIsValidRoute(List.of(bus, uber, flight));

        assertFalse(result);
    }

    @Test
    void isTransportationValidForDate_WithValidOperatingDays_ShouldReturnTrue() {
        Transportation flight = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(new Integer[]{4})
                .build();
        flight.setId(1L);

        boolean result = testableRouteStrategy.testIsTransportationValidForDate(flight,
                LocalDate.of(2025, 9, 11));

        assertTrue(result);
    }

    @Test
    void isTransportationValidForDate_WithInvalidOperatingDays_ShouldReturnFalse() {
        Transportation flight = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(new Integer[]{1, 2, 3})
                .build();
        flight.setId(1L);

        boolean result = testableRouteStrategy.testIsTransportationValidForDate(flight,
                LocalDate.of(2025, 9, 13));

        assertFalse(result);
    }

    @Test
    void isTransportationValidForDate_WithNullOperatingDays_ShouldReturnTrue() {
        Transportation flight = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(null)
                .build();
        flight.setId(1L);

        boolean result = testableRouteStrategy.testIsTransportationValidForDate(flight,
                LocalDate.of(2024, 12, 25));

        assertTrue(result);
    }

    @Test
    void isTransportationValidForDate_WithEmptyOperatingDays_ShouldReturnTrue() {
        Transportation flight = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(new Integer[]{})
                .build();
        flight.setId(1L);

        boolean result = testableRouteStrategy.testIsTransportationValidForDate(flight,
                LocalDate.of(2024, 12, 25));

        assertTrue(result);
    }

    @Test
    void isTransportationValidForDate_WithNullDate_ShouldReturnTrue() {
        Transportation flight = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(new Integer[]{1, 2, 3})
                .build();
        flight.setId(1L);

        boolean result = testableRouteStrategy.testIsTransportationValidForDate(flight, null);

        assertTrue(result);
    }

    private static class TestableRouteStrategy extends AbstractRouteStrategy {
        public TestableRouteStrategy(TransportationRepository transportationRepository) {
            super(transportationRepository);
        }

        @Override
        public List<RouteDTO> findRoutes(
                Location origin, Location destination, LocalDate date) {
            return List.of();
        }

        @Override
        public int getMaxTransportations() {
            return 1;
        }

        public boolean testIsValidRoute(List<Transportation> transportations) {
            return isValidRoute(transportations);
        }

        public boolean testIsTransportationValidForDate(Transportation transportation, LocalDate date) {
            return isTransportationValidForDate(transportation, date);
        }
    }
}
