package com.turkishairlines.routeplanning.service.strategy;

import com.turkishairlines.routeplanning.model.dto.LocationDTO;
import com.turkishairlines.routeplanning.model.dto.TransportationDTO;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import com.turkishairlines.routeplanning.repository.TransportationJpaRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public abstract class AbstractRouteStrategy implements RouteStrategy {

    protected final TransportationJpaRepository transportationRepository;

    protected boolean isValidRoute(List<Transportation> transportations) {
        if (transportations.isEmpty() || transportations.size() > 3) {
            return false;
        }

        // Count transportation types
        long flightCount = transportations.stream()
                .mapToLong(t -> t.getTransportationType() == TransportationType.FLIGHT ? 1 : 0)
                .sum();

        // Must have exactly one flight
        if (flightCount != 1) {
            return false;
        }

        // Find flight position
        int flightIndex = -1;
        for (int i = 0; i < transportations.size(); i++) {
            if (transportations.get(i).getTransportationType() == TransportationType.FLIGHT) {
                flightIndex = i;
                break;
            }
        }

        // Validate route structure based on flight position
        if (transportations.size() == 1) {
            // Single flight is valid
            return true;
        } else if (transportations.size() == 2) {
            // Two transportations: either TRANSFER->FLIGHT or FLIGHT->TRANSFER
            return flightIndex == 0 || flightIndex == 1;
        } else if (transportations.size() == 3) {
            // Three transportations: must be TRANSFER->FLIGHT->TRANSFER
            if (flightIndex != 1) {
                return false;
            }

            // Before flight transfer (must not be FLIGHT)
            TransportationType beforeFlight = transportations.get(0).getTransportationType();
            // After flight transfer (must not be FLIGHT)
            TransportationType afterFlight = transportations.get(2).getTransportationType();

            return beforeFlight != TransportationType.FLIGHT && afterFlight != TransportationType.FLIGHT;
        }

        return false;
    }

    protected boolean isTransportationValidForDate(Transportation transportation, LocalDate date) {
        if (date == null || transportation.getOperatingDays() == null
                || transportation.getOperatingDays().length == 0) {
            return true; // If no date specified or no operating days restriction, consider it valid
        }

        int dayOfWeek = date.getDayOfWeek().getValue(); // Monday = 1, Sunday = 7
        return Arrays.stream(transportation.getOperatingDays())
                .anyMatch(day -> day == dayOfWeek);
    }

    protected LocationDTO convertLocationToDTO(Location location) {
        return LocationDTO.builder()
                .id(location.getId())
                .name(location.getName())
                .country(location.getCountry())
                .city(location.getCity())
                .locationCode(location.getLocationCode())
                .build();
    }

    protected TransportationDTO convertTransportationToDTO(Transportation transportation) {
        return TransportationDTO.builder()
                .id(transportation.getId())
                .originLocationId(transportation.getOriginLocation().getId())
                .destinationLocationId(transportation.getDestinationLocation().getId())
                .transportationType(transportation.getTransportationType())
                .operatingDays(transportation.getOperatingDays())
                .originLocation(convertLocationToDTO(transportation.getOriginLocation()))
                .destinationLocation(convertLocationToDTO(transportation.getDestinationLocation()))
                .build();
    }
}
