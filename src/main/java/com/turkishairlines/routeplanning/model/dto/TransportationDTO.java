package com.turkishairlines.routeplanning.model.dto;

import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportationDTO {

    private Long id;

    @NotNull(message = "Origin location ID is required")
    private Long originLocationId;

    @NotNull(message = "Destination location ID is required")
    private Long destinationLocationId;

    @NotNull(message = "Transportation type is required")
    private TransportationType transportationType;

    private Integer[] operatingDays;

    // For response purposes
    private LocationDTO originLocation;
    private LocationDTO destinationLocation;
}
