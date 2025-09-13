package com.turkishairlines.routeplanning.service.strategy;

import com.turkishairlines.routeplanning.model.dto.RouteDTO;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import com.turkishairlines.routeplanning.repository.TransportationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TwoStepRouteStrategyTest {

        @Mock
        private TransportationJpaRepository transportationRepository;

        private TwoStepRouteStrategy twoStepRouteStrategy;

        private Location taksimSquare;
        private Location istanbulAirport;
        private Location londonHeathrow;
        private Location wembleyStadium;

        @BeforeEach
        void setUp() {
                twoStepRouteStrategy = new TwoStepRouteStrategy(transportationRepository);

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
        void findRoutes_WithValidTwoStepRoute_ShouldReturnRoute() {
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

                when(transportationRepository.findByOriginLocation(taksimSquare))
                                .thenReturn(List.of(busToAirport));
                when(transportationRepository.findByDestinationLocation(londonHeathrow))
                                .thenReturn(List.of(flight));

                List<RouteDTO> routes = twoStepRouteStrategy.findRoutes(taksimSquare, londonHeathrow,
                                LocalDate.of(2024, 12, 25));

                assertNotNull(routes);
                assertEquals(1, routes.size());

                RouteDTO route = routes.get(0);
                assertEquals(2, route.getTotalTransportations());
                assertEquals(TransportationType.BUS, route.getTransportations().get(0).getTransportationType());
                assertEquals(TransportationType.FLIGHT, route.getTransportations().get(1).getTransportationType());
        }

        @Test
        void findRoutes_WithFlightFirstThenTransfer_ShouldReturnRoute() {
                Transportation flight = Transportation.builder()
                                .originLocation(istanbulAirport)
                                .destinationLocation(londonHeathrow)
                                .transportationType(TransportationType.FLIGHT)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                flight.setId(1L);

                Transportation uberToStadium = Transportation.builder()
                                .originLocation(londonHeathrow)
                                .destinationLocation(wembleyStadium)
                                .transportationType(TransportationType.UBER)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                uberToStadium.setId(2L);

                when(transportationRepository.findByOriginLocation(istanbulAirport))
                                .thenReturn(List.of(flight));
                when(transportationRepository.findByDestinationLocation(wembleyStadium))
                                .thenReturn(List.of(uberToStadium));

                List<RouteDTO> routes = twoStepRouteStrategy.findRoutes(istanbulAirport, wembleyStadium,
                                LocalDate.of(2024, 12, 25));

                assertNotNull(routes);
                assertEquals(1, routes.size());

                RouteDTO route = routes.get(0);
                assertEquals(2, route.getTotalTransportations());
                assertEquals(TransportationType.FLIGHT, route.getTransportations().get(0).getTransportationType());
                assertEquals(TransportationType.UBER, route.getTransportations().get(1).getTransportationType());
        }

        @Test
        void findRoutes_WithInvalidRouteStructure_ShouldFilterOut() {
                Transportation busToAirport = Transportation.builder()
                                .originLocation(taksimSquare)
                                .destinationLocation(istanbulAirport)
                                .transportationType(TransportationType.BUS)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                busToAirport.setId(1L);

                Transportation busToStadium = Transportation.builder()
                                .originLocation(istanbulAirport)
                                .destinationLocation(wembleyStadium)
                                .transportationType(TransportationType.BUS)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                busToStadium.setId(2L);

                when(transportationRepository.findByOriginLocation(taksimSquare))
                                .thenReturn(List.of(busToAirport));
                when(transportationRepository.findByDestinationLocation(wembleyStadium))
                                .thenReturn(List.of(busToStadium));

                List<RouteDTO> routes = twoStepRouteStrategy.findRoutes(taksimSquare, wembleyStadium,
                                LocalDate.of(2024, 12, 25));

                assertNotNull(routes);
                assertTrue(routes.isEmpty());
        }

        @Test
        void findRoutes_WithNoConnectingTransportation_ShouldReturnEmptyList() {
                Transportation busToAirport = Transportation.builder()
                                .originLocation(taksimSquare)
                                .destinationLocation(istanbulAirport)
                                .transportationType(TransportationType.BUS)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                busToAirport.setId(1L);

                when(transportationRepository.findByOriginLocation(taksimSquare))
                                .thenReturn(List.of(busToAirport));
                when(transportationRepository.findByDestinationLocation(wembleyStadium))
                                .thenReturn(Collections.emptyList());

                List<RouteDTO> routes = twoStepRouteStrategy.findRoutes(taksimSquare, wembleyStadium,
                                LocalDate.of(2024, 12, 25));

                assertNotNull(routes);
                assertTrue(routes.isEmpty());
        }

        @Test
        void getMaxTransportations_ShouldReturnTwo() {
                int maxTransportations = twoStepRouteStrategy.getMaxTransportations();

                assertEquals(2, maxTransportations);
        }
}
