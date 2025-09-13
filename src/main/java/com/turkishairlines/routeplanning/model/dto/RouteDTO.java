package com.turkishairlines.routeplanning.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDTO {

    private LocationDTO originLocation;
    private LocationDTO destinationLocation;
    private List<TransportationDTO> transportations;
    private int totalTransportations;

    // Helper method to get route description
    public String getRouteDescription() {
        if (transportations == null || transportations.isEmpty()) {
            return "No route";
        }

        StringBuilder description = new StringBuilder();
        for (int i = 0; i < transportations.size(); i++) {
            TransportationDTO transport = transportations.get(i);
            if (i > 0) {
                description.append(" ➡ ");
            }
            description.append(transport.getTransportationType());
        }

        return description.toString();
    }
}
