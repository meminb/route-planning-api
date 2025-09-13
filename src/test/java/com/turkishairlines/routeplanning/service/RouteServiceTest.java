package com.turkishairlines.routeplanning.service;

import com.turkishairlines.routeplanning.model.dto.RouteDTO;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import com.turkishairlines.routeplanning.repository.LocationRepository;
import com.turkishairlines.routeplanning.repository.TransportationCriteriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private TransportationCriteriaRepository transportationRepository;

    @InjectMocks
    private RouteService routeService;

    private Location taksimSquare;
    private Location istanbulAirport;
    private Location londonHeathrow;
    private Location wembleyStadium;

    @BeforeEach
    void setUp() {
        // Create test locations
        taksimSquare = Location.builder()
                .name("Taksim Square")
                .city("Istanbul")
                .country("Turkey")
                .locationCode("TSQ")
                .build();
        taksimSquare.setId(1L);

        istanbulAirport = Location.builder()
                .name("Istanbul Airport")
                .city("Istanbul")
                .country("Turkey")
                .locationCode("IST")
                .build();
        istanbulAirport.setId(2L);

        londonHeathrow = Location.builder()
                .name("London Heathrow Airport")
                .city("London")
                .country("UK")
                .locationCode("LHR")
                .build();
        londonHeathrow.setId(3L);

        wembleyStadium = Location.builder()
                .name("Wembley Stadium")
                .city("London")
                .country("UK")
                .locationCode("WS")
                .build();
        wembleyStadium.setId(4L);

    }

    @Test
    void findValidRoutes_DirectFlight_ShouldReturnValidRoute() {
        // Given
        when(locationRepository.findByLocationCode("IST")).thenReturn(Optional.of(istanbulAirport));
        when(locationRepository.findByLocationCode("LHR")).thenReturn(Optional.of(londonHeathrow));

        Transportation directFlight = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .build();
    directFlight.setId(1L);
        when(transportationRepository.findByOriginLocationAndDestinationLocation(istanbulAirport, londonHeathrow))
                .thenReturn(List.of(directFlight));
        when(transportationRepository.findByOriginLocation(any())).thenReturn(List.of());
        when(transportationRepository.findByDestinationLocation(any())).thenReturn(List.of());

        // When
        List<RouteDTO> routes = routeService.findValidRoutes("IST", "LHR");

        // Then
        assertNotNull(routes);
        assertEquals(1, routes.size());

        RouteDTO route = routes.get(0);
        assertEquals(1, route.getTotalTransportations());
        assertEquals(TransportationType.FLIGHT, route.getTransportations().get(0).getTransportationType());
        assertEquals("FLIGHT", route.getRouteDescription());
    }

    @Test
    void findValidRoutes_ThreeStepRoute_ShouldReturnValidRoute() {
        // Given
        when(locationRepository.findByLocationCode("TSQ")).thenReturn(Optional.of(taksimSquare));
        when(locationRepository.findByLocationCode("WS")).thenReturn(Optional.of(wembleyStadium));

        // Create transportations for: TSQ -> IST -> LHR -> WS
        Transportation busToAirport = Transportation.builder()
                .originLocation(taksimSquare)
                .destinationLocation(istanbulAirport)
                .transportationType(TransportationType.BUS)
                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 }) // All days
                .build();
        busToAirport.setId(1L);

        Transportation flight = Transportation.builder()
                .originLocation(istanbulAirport)
                .destinationLocation(londonHeathrow)
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 }) // All days
                .build();
        flight.setId(2L);

        Transportation uberToStadium = Transportation.builder()
                .originLocation(londonHeathrow)
                .destinationLocation(wembleyStadium)
                .transportationType(TransportationType.UBER)
                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 }) // All days
                .build();
        uberToStadium.setId(3L);

        // Mock repository calls
        when(transportationRepository.findByOriginLocationAndDestinationLocation(taksimSquare, wembleyStadium))
                .thenReturn(List.of());
        when(transportationRepository.findByOriginLocation(taksimSquare))
                .thenReturn(List.of(busToAirport));
        when(transportationRepository.findByDestinationLocation(wembleyStadium))
                .thenReturn(List.of(uberToStadium));
        when(transportationRepository.findByOriginLocation(istanbulAirport))
                .thenReturn(List.of(flight));
        when(transportationRepository.findByOriginLocationAndDestinationLocation(londonHeathrow, wembleyStadium))
                .thenReturn(List.of(uberToStadium));

        // When
        List<RouteDTO> routes = routeService.findValidRoutes("TSQ", "WS", LocalDate.of(2024, 12, 25)); // Wednesday

        // Then
        assertNotNull(routes);
        assertEquals(1, routes.size());

        RouteDTO route = routes.get(0);
        assertEquals(3, route.getTotalTransportations());
        assertEquals("BUS ➡ FLIGHT ➡ UBER", route.getRouteDescription());

        List<TransportationType> expectedTypes = Arrays.asList(
                TransportationType.BUS,
                TransportationType.FLIGHT,
                TransportationType.UBER);

        for (int i = 0; i < expectedTypes.size(); i++) {
            assertEquals(expectedTypes.get(i), route.getTransportations().get(i).getTransportationType());
        }
    }
}
