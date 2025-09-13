package com.turkishairlines.routeplanning.service.strategy;

import com.turkishairlines.routeplanning.model.dto.RouteDTO;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.repository.TransportationJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ThreeStepRouteStrategy extends AbstractRouteStrategy {

    public ThreeStepRouteStrategy(TransportationJpaRepository transportationRepository) {
        super(transportationRepository);
    }

    @Override
    public List<RouteDTO> findRoutes(Location origin, Location destination, LocalDate date) {
        log.debug("Finding three-step routes from {} to {}", origin.getLocationCode(), destination.getLocationCode());

        List<RouteDTO> routes = new ArrayList<>();

        // Get all transportations from origin
        List<Transportation> fromOrigin = transportationRepository.findByOriginLocation(origin)
                .stream()
                .filter(t -> isTransportationValidForDate(t, date))
                .toList();

        for (Transportation first : fromOrigin) {
            Location intermediateLocation1 = first.getDestinationLocation();

            // Get transportations from first intermediate location
            List<Transportation> fromIntermediate1 = transportationRepository
                    .findByOriginLocation(intermediateLocation1)
                    .stream()
                    .filter(t -> isTransportationValidForDate(t, date))
                    .collect(Collectors.toList());

            for (Transportation second : fromIntermediate1) {
                Location intermediateLocation2 = second.getDestinationLocation();

                // Get transportations from second intermediate location to destination
                List<Transportation> toDestination = transportationRepository
                        .findByOriginLocationAndDestinationLocation(intermediateLocation2, destination)
                        .stream()
                        .filter(t -> isTransportationValidForDate(t, date))
                        .collect(Collectors.toList());

                for (Transportation third : toDestination) {
                    List<Transportation> routeTransportations = Arrays.asList(first, second, third);

                    if (isValidRoute(routeTransportations)) {
                        routes.add(RouteDTO.builder()
                                .originLocation(convertLocationToDTO(origin))
                                .destinationLocation(convertLocationToDTO(destination))
                                .transportations(routeTransportations.stream()
                                        .map(this::convertTransportationToDTO)
                                        .collect(Collectors.toList()))
                                .totalTransportations(3)
                                .build());
                    }
                }
            }
        }

        return routes;
    }

    @Override
    public int getMaxTransportations() {
        return 3;
    }
}
