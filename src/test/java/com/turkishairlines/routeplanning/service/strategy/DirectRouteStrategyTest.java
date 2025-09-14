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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectRouteStrategyTest {

        @Mock
        private TransportationRepository transportationRepository;

        private DirectRouteStrategy directRouteStrategy;

        private Location istanbulAirport;
        private Location londonHeathrow;

        @BeforeEach
        void setUp() {
                directRouteStrategy = new DirectRouteStrategy(transportationRepository);

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
        void findRoutes_WithValidDirectFlight_ShouldReturnRoute() {
                Transportation flight = Transportation.builder()
                                .originLocation(istanbulAirport)
                                .destinationLocation(londonHeathrow)
                                .transportationType(TransportationType.FLIGHT)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                flight.setId(1L);

                when(transportationRepository.findByOriginLocationAndDestinationLocation(istanbulAirport,
                                londonHeathrow))
                                .thenReturn(List.of(flight));

                List<RouteDTO> routes = directRouteStrategy.findRoutes(istanbulAirport, londonHeathrow,
                                LocalDate.of(2024, 12, 25));

                assertNotNull(routes);
                assertEquals(1, routes.size());

                RouteDTO route = routes.get(0);
                assertEquals(1, route.getTotalTransportations());
                assertEquals(TransportationType.FLIGHT, route.getTransportations().get(0).getTransportationType());
                assertEquals(istanbulAirport.getLocationCode(), route.getOriginLocation().getLocationCode());
                assertEquals(londonHeathrow.getLocationCode(), route.getDestinationLocation().getLocationCode());
        }

        @Test
        void findRoutes_WithNonFlightTransportation_ShouldFilterOut() {
                Transportation bus = Transportation.builder()
                                .originLocation(istanbulAirport)
                                .destinationLocation(londonHeathrow)
                                .transportationType(TransportationType.BUS)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                bus.setId(1L);

                Transportation flight = Transportation.builder()
                                .originLocation(istanbulAirport)
                                .destinationLocation(londonHeathrow)
                                .transportationType(TransportationType.FLIGHT)
                                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 })
                                .build();
                flight.setId(2L);

                when(transportationRepository.findByOriginLocationAndDestinationLocation(istanbulAirport,
                                londonHeathrow))
                                .thenReturn(List.of(bus, flight));

                List<RouteDTO> routes = directRouteStrategy.findRoutes(istanbulAirport, londonHeathrow,
                                LocalDate.of(2024, 12, 25));

                assertNotNull(routes);
                assertEquals(1, routes.size());
                assertEquals(TransportationType.FLIGHT,
                                routes.get(0).getTransportations().get(0).getTransportationType());
        }

        @Test
        void findRoutes_WithNoDirectTransportation_ShouldReturnEmptyList() {
                when(transportationRepository.findByOriginLocationAndDestinationLocation(istanbulAirport,
                                londonHeathrow))
                                .thenReturn(Collections.emptyList());

                List<RouteDTO> routes = directRouteStrategy.findRoutes(istanbulAirport, londonHeathrow,
                                LocalDate.of(2024, 12, 25));

                assertNotNull(routes);
                assertTrue(routes.isEmpty());
        }

        @Test
        void getMaxTransportations_ShouldReturnOne() {
                int maxTransportations = directRouteStrategy.getMaxTransportations();

                assertEquals(1, maxTransportations);
        }
}
