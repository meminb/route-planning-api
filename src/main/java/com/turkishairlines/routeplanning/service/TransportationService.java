package com.turkishairlines.routeplanning.service;

import com.turkishairlines.routeplanning.model.dto.LocationDTO;
import com.turkishairlines.routeplanning.model.dto.TransportationDTO;
import com.turkishairlines.routeplanning.exception.ResourceNotFoundException;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import com.turkishairlines.routeplanning.repository.LocationRepository;
import com.turkishairlines.routeplanning.repository.TransportationCriteriaRepository;
import com.turkishairlines.routeplanning.repository.TransportationJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransportationService {

    private final TransportationCriteriaRepository transportationCriteriaRepository;
    private final TransportationJpaRepository transportationRepository;
    private final LocationRepository locationRepository;

    public Page<TransportationDTO> findAllWithFilters(Pageable pageable, Long originLocationId,
            Long destinationLocationId, TransportationType transportationType) {
        log.debug("Finding transportations with filters - origin: {}, destination: {}, type: {}",
                originLocationId, destinationLocationId, transportationType);

        Page<Transportation> transportations = transportationCriteriaRepository.findAllWithFilters(
                pageable, originLocationId, destinationLocationId, transportationType);

        return transportations.map(this::convertToDTO);
    }

    public TransportationDTO findById(Long id) {
        log.debug("Finding transportation by id: {}", id);
        Transportation transportation = transportationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transportation not found with id: " + id));
        return convertToDTO(transportation);
    }

    @Transactional
    public TransportationDTO create(TransportationDTO transportationDTO) {
        log.debug("Creating new transportation: {}", transportationDTO);

        Location originLocation = locationRepository.findById(transportationDTO.getOriginLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Origin location not found with id: " + transportationDTO.getOriginLocationId()));

        Location destinationLocation = locationRepository.findById(transportationDTO.getDestinationLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Destination location not found with id: " + transportationDTO.getDestinationLocationId()));

        Transportation transportation = Transportation.builder()
                .originLocation(originLocation)
                .destinationLocation(destinationLocation)
                .transportationType(transportationDTO.getTransportationType())
                .operatingDays(transportationDTO.getOperatingDays())
                .build();

        Transportation savedTransportation = transportationRepository.save(transportation);
        log.info("Created transportation with id: {}", savedTransportation.getId());

        return convertToDTO(savedTransportation);
    }

    @Transactional
    public TransportationDTO update(Long id, TransportationDTO transportationDTO) {
        log.debug("Updating transportation with id: {}", id);

        Transportation existingTransportation = transportationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transportation not found with id: " + id));

        Location originLocation = locationRepository.findById(transportationDTO.getOriginLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Origin location not found with id: " + transportationDTO.getOriginLocationId()));

        Location destinationLocation = locationRepository.findById(transportationDTO.getDestinationLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Destination location not found with id: " + transportationDTO.getDestinationLocationId()));

        existingTransportation.setOriginLocation(originLocation);
        existingTransportation.setDestinationLocation(destinationLocation);
        existingTransportation.setTransportationType(transportationDTO.getTransportationType());
        existingTransportation.setOperatingDays(transportationDTO.getOperatingDays());

        Transportation updatedTransportation = transportationRepository.save(existingTransportation);
        log.info("Updated transportation with id: {}", updatedTransportation.getId());

        return convertToDTO(updatedTransportation);
    }

    @Transactional
    public void delete(Long id) {
        log.debug("Deleting transportation with id: {}", id);

        Transportation transportation = transportationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transportation not found with id: " + id));

        transportation.setDeletedAt(Instant.now());
        transportationRepository.save(transportation);
        log.info("Soft deleted transportation with id: {}", id);
    }

    private TransportationDTO convertToDTO(Transportation transportation) {
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

    private LocationDTO convertLocationToDTO(Location location) {
        return LocationDTO.builder()
                .id(location.getId())
                .name(location.getName())
                .country(location.getCountry())
                .city(location.getCity())
                .locationCode(location.getLocationCode())
                .build();
    }
}
