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

        long flightCount = transportations.stream()
                .mapToLong(t -> t.getTransportationType() == TransportationType.FLIGHT ? 1 : 0)
                .sum();

        if (flightCount != 1) {
            return false;
        }
        int flightIndex = -1;
        for (int i = 0; i < transportations.size(); i++) {
            if (transportations.get(i).getTransportationType() == TransportationType.FLIGHT) {
                flightIndex = i;
                break;
            }
        }

        if (transportations.size() == 1) {
            return true;
        } else if (transportations.size() == 2) {
            return flightIndex == 0 || flightIndex == 1;
        } else if (transportations.size() == 3) {
            if (flightIndex != 1) {
                return false;
            }
            TransportationType beforeFlight = transportations.get(0).getTransportationType();
            TransportationType afterFlight = transportations.get(2).getTransportationType();
            return beforeFlight != TransportationType.FLIGHT && afterFlight != TransportationType.FLIGHT;
        }

        return false;
    }

    protected boolean isTransportationValidForDate(Transportation transportation, LocalDate date) {
        if (date == null || transportation.getOperatingDays() == null
                || transportation.getOperatingDays().length == 0) {
            return true;
        }

        int dayOfWeek = date.getDayOfWeek().getValue(); // pzt = 1, pazar = 7
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
