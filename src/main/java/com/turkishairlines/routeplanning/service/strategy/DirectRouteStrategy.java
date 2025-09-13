package com.turkishairlines.routeplanning.service.strategy;

import com.turkishairlines.routeplanning.model.dto.RouteDTO;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import com.turkishairlines.routeplanning.repository.TransportationJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DirectRouteStrategy extends AbstractRouteStrategy {

    public DirectRouteStrategy(TransportationJpaRepository transportationRepository) {
        super(transportationRepository);
    }

    @Override
    public List<RouteDTO> findRoutes(Location origin, Location destination, LocalDate date) {
        log.debug("Finding direct routes from {} to {}", origin.getLocationCode(), destination.getLocationCode());

        List<Transportation> directTransportations = transportationRepository
                .findByOriginLocationAndDestinationLocation(origin, destination);

        return directTransportations.stream()
                .filter(t -> isTransportationValidForDate(t, date))
                .filter(t -> t.getTransportationType() == TransportationType.FLIGHT) // Direct route must be a flight
                .map(transport -> RouteDTO.builder()
                        .originLocation(convertLocationToDTO(origin))
                        .destinationLocation(convertLocationToDTO(destination))
                        .transportations(List.of(convertTransportationToDTO(transport)))
                        .totalTransportations(1)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public int getMaxTransportations() {
        return 1;
    }
}
