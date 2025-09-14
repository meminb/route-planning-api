package com.turkishairlines.routeplanning.service.strategy;

import com.turkishairlines.routeplanning.model.dto.RouteDTO;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.repository.TransportationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TwoStepRouteStrategy extends AbstractRouteStrategy {

    public TwoStepRouteStrategy(TransportationRepository transportationRepository) {
        super(transportationRepository);
    }

    @Override
    public List<RouteDTO> findRoutes(Location origin, Location destination, LocalDate date) {
        log.debug("Finding two-step routes from {} to {}", origin.getLocationCode(), destination.getLocationCode());

        List<RouteDTO> routes = new ArrayList<>();

        List<Transportation> fromOrigin = transportationRepository.findByOriginLocation(origin)
                .stream()
                .filter(t -> isTransportationValidForDate(t, date))
                .toList();

        List<Transportation> toDestination = transportationRepository.findByDestinationLocation(destination)
                .stream()
                .filter(t -> isTransportationValidForDate(t, date))
                .toList();

        for (Transportation first : fromOrigin) {
            for (Transportation second : toDestination) {
                if (first.getDestinationLocation().getId().equals(second.getOriginLocation().getId())) {
                    List<Transportation> routeTransportations = Arrays.asList(first, second);

                    if (isValidRoute(routeTransportations)) {
                        routes.add(RouteDTO.builder()
                                .originLocation(convertLocationToDTO(origin))
                                .destinationLocation(convertLocationToDTO(destination))
                                .transportations(routeTransportations.stream()
                                        .map(this::convertTransportationToDTO)
                                        .collect(Collectors.toList()))
                                .totalTransportations(2)
                                .build());
                    }
                }
            }
        }

        return routes;
    }

    @Override
    public int getMaxTransportations() {
        return 2;
    }
}
