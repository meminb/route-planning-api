package com.turkishairlines.routeplanning.controller;

import com.turkishairlines.routeplanning.model.dto.LocationDTO;
import com.turkishairlines.routeplanning.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

        private final LocationService locationService;

        @GetMapping("/page")
        public ResponseEntity<Page<LocationDTO>> getAllLocationsWithPagination(Pageable pageable) {
                Page<LocationDTO> locations = locationService.findAll(pageable);
                return ResponseEntity.ok(locations);
        }

        @GetMapping("/{id}")
        public ResponseEntity<LocationDTO> getLocationById(@PathVariable Long id) {
                LocationDTO location = locationService.findById(id);
                return ResponseEntity.ok(location);
        }

        @GetMapping("/code/{locationCode}")
        public ResponseEntity<LocationDTO> getLocationByCode(@PathVariable String locationCode) {
                LocationDTO location = locationService.findByLocationCode(locationCode);
                return ResponseEntity.ok(location);
        }

        @PostMapping
        public ResponseEntity<LocationDTO> createLocation(@Valid @RequestBody LocationDTO locationDTO) {
                LocationDTO createdLocation = locationService.create(locationDTO);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdLocation);
        }

        @PutMapping("/{id}")
        public ResponseEntity<LocationDTO> updateLocation(
                        @PathVariable Long id,
                        @Valid @RequestBody LocationDTO locationDTO) {
                LocationDTO updatedLocation = locationService.update(id, locationDTO);
                return ResponseEntity.ok(updatedLocation);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
                locationService.delete(id);
                return ResponseEntity.noContent().build();
        }
}
