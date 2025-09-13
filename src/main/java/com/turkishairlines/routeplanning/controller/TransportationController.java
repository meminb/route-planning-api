package com.turkishairlines.routeplanning.controller;

import com.turkishairlines.routeplanning.model.dto.TransportationDTO;
import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import com.turkishairlines.routeplanning.service.TransportationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transportations")
@RequiredArgsConstructor
public class TransportationController {

    private final TransportationService transportationService;

    @GetMapping
    public ResponseEntity<Page<TransportationDTO>> getAllTransportationsWithPagination(
            Pageable pageable,
            @RequestParam(required = false) Long originId,
            @RequestParam(required = false) Long destinationId,
            @RequestParam(required = false) TransportationType transportationType) {
        Page<TransportationDTO> transportations = transportationService.findAllWithFilters(
                pageable, originId, destinationId, transportationType);
        return ResponseEntity.ok(transportations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransportationDTO> getTransportationById(@PathVariable Long id) {
        TransportationDTO transportation = transportationService.findById(id);
        return ResponseEntity.ok(transportation);
    }

    @PostMapping
    public ResponseEntity<TransportationDTO> createTransportation(
            @Valid @RequestBody TransportationDTO transportationDTO) {
        TransportationDTO createdTransportation = transportationService.create(transportationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransportation);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransportationDTO> updateTransportation(
            @PathVariable Long id,
            @Valid @RequestBody TransportationDTO transportationDTO) {
        TransportationDTO updatedTransportation = transportationService.update(id, transportationDTO);
        return ResponseEntity.ok(updatedTransportation);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransportation(@PathVariable Long id) {
        transportationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
