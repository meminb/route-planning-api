package com.turkishairlines.routeplanning.service.strategy;

import com.turkishairlines.routeplanning.model.dto.RouteDTO;
import com.turkishairlines.routeplanning.model.dto.TransportationDTO;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import com.turkishairlines.routeplanning.repository.TransportationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ThreeStepRouteStrategy extends AbstractRouteStrategy {

    public ThreeStepRouteStrategy(TransportationRepository repository) {
        super(repository);
    }

    @Override
    public List<RouteDTO> findRoutes(Location origin, Location destination, LocalDate date) {
        log.debug("3-step routes {} -> {}", origin.getLocationCode(), destination.getLocationCode());

        List<Transportation> nonFlightTransfersFromOrigin =
                transportationRepository.findByOriginLocationAndTransportationTypeNot(origin, TransportationType.FLIGHT)
                        .stream().filter(t -> isTransportationValidForDate(t, date)).toList();

        Set<Location> candidateFlightOriginHubs =
                nonFlightTransfersFromOrigin.stream()
                        .map(Transportation::getDestinationLocation)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        if (candidateFlightOriginHubs.isEmpty()) return List.of();

        List<Transportation> nonFlightTransfersToDestination =
                transportationRepository.findByDestinationLocationAndTransportationTypeNot(destination, TransportationType.FLIGHT)
                        .stream().filter(t -> isTransportationValidForDate(t, date)).toList();

        Set<Location> candidateFlightDestinationHubs =
                nonFlightTransfersToDestination.stream()
                        .map(Transportation::getOriginLocation)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        if (candidateFlightDestinationHubs.isEmpty()) return List.of();

        List<Transportation> flightsBetweenCandidateHubs =
                transportationRepository.findByTransportationTypeAndOriginLocationInAndDestinationLocationIn(
                                TransportationType.FLIGHT, candidateFlightOriginHubs, candidateFlightDestinationHubs)
                        .stream().filter(t -> isTransportationValidForDate(t, date)).toList();

        Map<Long, List<Transportation>> beforeTransfersByFlightOriginId =
                nonFlightTransfersFromOrigin.stream()
                        .collect(Collectors.groupingBy(t -> t.getDestinationLocation().getId()));

        Map<Long, List<Transportation>> afterTransfersByFlightDestinationId =
                nonFlightTransfersToDestination.stream()
                        .collect(Collectors.groupingBy(t -> t.getOriginLocation().getId()));

        List<RouteDTO> routes = new ArrayList<>();
        Set<String> routeSignatureSet = new HashSet<>();

        for (Transportation flight : flightsBetweenCandidateHubs) {
            Long flightOriginId = flight.getOriginLocation().getId();
            Long flightDestinationId = flight.getDestinationLocation().getId();

            List<Transportation> beforeCandidates =
                    beforeTransfersByFlightOriginId.getOrDefault(flightOriginId, List.of());
            List<Transportation> afterCandidates =
                    afterTransfersByFlightDestinationId.getOrDefault(flightDestinationId, List.of());

            for (Transportation beforeLeg : beforeCandidates) {
                for (Transportation afterLeg : afterCandidates) {
                    List<Transportation> legs = List.of(beforeLeg, flight, afterLeg);
                    if (!isValidRoute(legs)) continue;

                    String signature = beforeLeg.getId() + "-" + flight.getId() + "-" + afterLeg.getId();
                    if (!routeSignatureSet.add(signature)) continue;

                    routes.add(RouteDTO.builder()
                            .originLocation(convertLocationToDTO(origin))
                            .destinationLocation(convertLocationToDTO(destination))
                            .transportations(legs.stream().map(this::convertTransportationToDTO).toList())
                            .totalTransportations(3)
                            .build());
                }
            }
        }

        routes.sort(Comparator.comparing(r ->
                r.getTransportations().stream().map(TransportationDTO::getId).map(String::valueOf)
                        .collect(Collectors.joining(","))));

        return routes;
    }

    @Override
    public int getMaxTransportations() {
        return 3;
    }
}
