package com.turkishairlines.routeplanning.service.strategy;

import com.turkishairlines.routeplanning.model.dto.RouteDTO;
import com.turkishairlines.routeplanning.model.entity.Location;

import java.time.LocalDate;
import java.util.List;

public interface RouteStrategy {

    List<RouteDTO> findRoutes(Location origin, Location destination, LocalDate date);

    int getMaxTransportations();
}
