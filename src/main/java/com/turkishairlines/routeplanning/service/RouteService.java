package com.turkishairlines.routeplanning.service;

import com.turkishairlines.routeplanning.model.dto.RouteDTO;
import com.turkishairlines.routeplanning.exception.InvalidRouteException;
import com.turkishairlines.routeplanning.exception.ResourceNotFoundException;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.repository.LocationRepository;
import com.turkishairlines.routeplanning.service.strategy.RouteStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteService {

    private final LocationRepository locationRepository;
    private final List<RouteStrategy> routeStrategies;

    public List<RouteDTO> findValidRoutes(String originLocationCode, String destinationLocationCode) {
        return findValidRoutes(originLocationCode, destinationLocationCode, null);
    }

    public List<RouteDTO> findValidRoutes(String originLocationCode, String destinationLocationCode, LocalDate date) {
        log.debug("Finding valid routes from {} to {} on date {}", originLocationCode, destinationLocationCode, date);

        Location originLocation = locationRepository.findByLocationCode(originLocationCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Origin location not found with code: " + originLocationCode));

        Location destinationLocation = locationRepository.findByLocationCode(destinationLocationCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Destination location not found with code: " + destinationLocationCode));

        if (originLocation.getId().equals(destinationLocation.getId())) {
            throw new InvalidRouteException("Origin and destination cannot be the same location");
        }

        List<RouteDTO> validRoutes = new ArrayList<>();

        for (RouteStrategy strategy : routeStrategies) {
            List<RouteDTO> strategyRoutes = strategy.findRoutes(originLocation, destinationLocation, date);
            validRoutes.addAll(strategyRoutes);
            log.debug("Strategy {} found {} routes", strategy.getClass().getSimpleName(), strategyRoutes.size());
        }

        log.info("Found {} valid routes from {} to {}", validRoutes.size(), originLocationCode,
                destinationLocationCode);
        return validRoutes;
    }
}
