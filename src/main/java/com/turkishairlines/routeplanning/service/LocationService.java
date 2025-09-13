package com.turkishairlines.routeplanning.service;

import com.turkishairlines.routeplanning.exception.DuplicateResourceException;
import com.turkishairlines.routeplanning.exception.ResourceNotFoundException;
import com.turkishairlines.routeplanning.model.dto.LocationDTO;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final LocationRepository locationRepository;

    public Page<LocationDTO> findAll(Pageable pageable) {
        log.debug("Finding all locations with pagination: {}", pageable);
        return locationRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public LocationDTO findById(Long id) {
        log.debug("Finding location by id: {}", id);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
        return convertToDTO(location);
    }

    public LocationDTO findByLocationCode(String locationCode) {
        log.debug("Finding location by location code: {}", locationCode);
        Location location = locationRepository.findByLocationCode(locationCode)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with code: " + locationCode));
        return convertToDTO(location);
    }

    @Transactional
    public LocationDTO create(LocationDTO locationDTO) {
        log.debug("Creating new location: {}", locationDTO);

        if (locationRepository.existsByLocationCode(locationDTO.getLocationCode())) {
            throw new DuplicateResourceException(
                    "Location with code '" + locationDTO.getLocationCode() + "' already exists");
        }

        Location location = convertToEntity(locationDTO);
        Location savedLocation = locationRepository.save(location);
        log.info("Created location with id: {}", savedLocation.getId());

        return convertToDTO(savedLocation);
    }

    @Transactional
    public LocationDTO update(Long id, LocationDTO locationDTO) {
        log.debug("Updating location with id: {}", id);

        Location existingLocation = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        if (!existingLocation.getLocationCode().equals(locationDTO.getLocationCode()) &&
                locationRepository.existsByLocationCode(locationDTO.getLocationCode())) {
            throw new DuplicateResourceException(
                    "Location with code '" + locationDTO.getLocationCode() + "' already exists");
        }

        existingLocation.setName(locationDTO.getName());
        existingLocation.setCountry(locationDTO.getCountry());
        existingLocation.setCity(locationDTO.getCity());
        existingLocation.setLocationCode(locationDTO.getLocationCode());

        Location updatedLocation = locationRepository.save(existingLocation);
        log.info("Updated location with id: {}", updatedLocation.getId());

        return convertToDTO(updatedLocation);
    }

    @Transactional
    public void delete(Long id) {
        log.debug("Deleting location with id: {}", id);

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

        location.setDeletedAt(Instant.now());
        locationRepository.save(location);
        log.info("Soft deleted location with id: {}", id);
    }

    private LocationDTO convertToDTO(Location location) {
        return LocationDTO.builder()
                .id(location.getId())
                .name(location.getName())
                .country(location.getCountry())
                .city(location.getCity())
                .locationCode(location.getLocationCode())
                .build();
    }

    private Location convertToEntity(LocationDTO locationDTO) {
        return Location.builder()
                .name(locationDTO.getName())
                .country(locationDTO.getCountry())
                .city(locationDTO.getCity())
                .locationCode(locationDTO.getLocationCode())
                .build();
    }
}
