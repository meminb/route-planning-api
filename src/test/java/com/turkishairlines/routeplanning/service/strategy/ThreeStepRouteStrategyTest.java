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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThreeStepRouteStrategyTest {

        @Mock
        private TransportationRepository transportationRepository;

        private ThreeStepRouteStrategy threeStepRouteStrategy;

        private Location taksimSquare;
        private Location istanbulAirport;
        private Location londonHeathrow;
        private Location wembleyStadium;

        @BeforeEach
        void setUp() {
                threeStepRouteStrategy = new ThreeStepRouteStrategy(transportationRepository);

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
        void findRoutes_WithValidThreeStepRoute_ShouldReturnRoute() {
                Transportation busToAirport = Transportation.builder()
                                .originLocation(taksimSquare)
                                .destinationLocation(istanbulAirport)
                                .transportationType(TransportationType.BUS)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                busToAirport.setId(1L);

                Transportation flight = Transportation.builder()
                                .originLocation(istanbulAirport)
                                .destinationLocation(londonHeathrow)
                                .transportationType(TransportationType.FLIGHT)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                flight.setId(2L);

                Transportation uberToStadium = Transportation.builder()
                                .originLocation(londonHeathrow)
                                .destinationLocation(wembleyStadium)
                                .transportationType(TransportationType.UBER)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                uberToStadium.setId(3L);

                when(transportationRepository.findByOriginLocationAndTransportationTypeNot(taksimSquare,
                                TransportationType.FLIGHT))
                                .thenReturn(List.of(busToAirport));
                when(transportationRepository.findByDestinationLocationAndTransportationTypeNot(wembleyStadium,
                                TransportationType.FLIGHT))
                                .thenReturn(List.of(uberToStadium));
                when(transportationRepository.findByTransportationTypeAndOriginLocationInAndDestinationLocationIn(
                                eq(TransportationType.FLIGHT), any(), any()))
                                .thenReturn(List.of(flight));

                List<RouteDTO> routes = threeStepRouteStrategy.findRoutes(taksimSquare, wembleyStadium,
                                LocalDate.of(2024, 12, 25));

                assertNotNull(routes);
                assertEquals(1, routes.size());

                RouteDTO route = routes.get(0);
                assertEquals(3, route.getTotalTransportations());
                assertEquals(TransportationType.BUS, route.getTransportations().get(0).getTransportationType());
                assertEquals(TransportationType.FLIGHT, route.getTransportations().get(1).getTransportationType());
                assertEquals(TransportationType.UBER, route.getTransportations().get(2).getTransportationType());
        }

        @Test
        void findRoutes_WithNoBeforeTransfers_ShouldReturnEmptyList() {
                when(transportationRepository.findByOriginLocationAndTransportationTypeNot(taksimSquare,
                                TransportationType.FLIGHT))
                                .thenReturn(Collections.emptyList());

                List<RouteDTO> routes = threeStepRouteStrategy.findRoutes(taksimSquare, wembleyStadium,
                                LocalDate.of(2024, 12, 25));

                assertNotNull(routes);
                assertTrue(routes.isEmpty());
        }

        @Test
        void findRoutes_WithNoAfterTransfers_ShouldReturnEmptyList() {
                Transportation busToAirport = Transportation.builder()
                                .originLocation(taksimSquare)
                                .destinationLocation(istanbulAirport)
                                .transportationType(TransportationType.BUS)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                busToAirport.setId(1L);

                when(transportationRepository.findByOriginLocationAndTransportationTypeNot(taksimSquare,
                                TransportationType.FLIGHT))
                                .thenReturn(List.of(busToAirport));
                when(transportationRepository.findByDestinationLocationAndTransportationTypeNot(wembleyStadium,
                                TransportationType.FLIGHT))
                                .thenReturn(Collections.emptyList());

                List<RouteDTO> routes = threeStepRouteStrategy.findRoutes(taksimSquare, wembleyStadium,
                                LocalDate.of(2024, 12, 25));

                assertNotNull(routes);
                assertTrue(routes.isEmpty());
        }

        @Test
        void findRoutes_WithNoFlightsBetweenHubs_ShouldReturnEmptyList() {
                Transportation busToAirport = Transportation.builder()
                                .originLocation(taksimSquare)
                                .destinationLocation(istanbulAirport)
                                .transportationType(TransportationType.BUS)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                busToAirport.setId(1L);

                Transportation uberToStadium = Transportation.builder()
                                .originLocation(londonHeathrow)
                                .destinationLocation(wembleyStadium)
                                .transportationType(TransportationType.UBER)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                uberToStadium.setId(2L);

                when(transportationRepository.findByOriginLocationAndTransportationTypeNot(taksimSquare,
                                TransportationType.FLIGHT))
                                .thenReturn(List.of(busToAirport));
                when(transportationRepository.findByDestinationLocationAndTransportationTypeNot(wembleyStadium,
                                TransportationType.FLIGHT))
                                .thenReturn(List.of(uberToStadium));
                when(transportationRepository.findByTransportationTypeAndOriginLocationInAndDestinationLocationIn(
                                eq(TransportationType.FLIGHT), any(), any()))
                                .thenReturn(Collections.emptyList());

                List<RouteDTO> routes = threeStepRouteStrategy.findRoutes(taksimSquare, wembleyStadium,
                                LocalDate.of(2024, 12, 25));

                assertNotNull(routes);
                assertTrue(routes.isEmpty());
        }

        @Test
        void getMaxTransportations_ShouldReturnThree() {
                int maxTransportations = threeStepRouteStrategy.getMaxTransportations();

                assertEquals(3, maxTransportations);
        }
}
