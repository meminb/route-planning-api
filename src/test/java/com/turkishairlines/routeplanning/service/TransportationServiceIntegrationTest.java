package com.turkishairlines.routeplanning.service;

import com.turkishairlines.routeplanning.exception.ResourceNotFoundException;
import com.turkishairlines.routeplanning.model.dto.TransportationDTO;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import com.turkishairlines.routeplanning.repository.LocationRepository;
import com.turkishairlines.routeplanning.repository.TransportationJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class TransportationServiceIntegrationTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private TransportationService transportationService;

    @Autowired
    private TransportationJpaRepository transportationRepository;

    @Autowired
    private LocationRepository locationRepository;

    private Location istanbulLocation;
    private Location ankaraLocation;
    private Location izmirLocation;

    @BeforeEach
    void setUp() {
        transportationRepository.deleteAll();
        locationRepository.deleteAll();

        // Create test locations
        istanbulLocation = createTestLocation("Istanbul Airport", "Turkey", "Istanbul", "IST");
        ankaraLocation = createTestLocation("Ankara Airport", "Turkey", "Ankara", "ESB");
        izmirLocation = createTestLocation("Izmir Airport", "Turkey", "Izmir", "ADB");
    }

    @Test
    @DisplayName("Should find all transportations with filters")
    void shouldFindAllTransportationsWithFilters() {
        // Given
        createTestTransportation(istanbulLocation, ankaraLocation, TransportationType.FLIGHT,
                new Integer[] { 1, 2, 3, 4, 5 });
        createTestTransportation(istanbulLocation, izmirLocation, TransportationType.BUS,
                new Integer[] { 1, 2, 3, 4, 5, 6, 7 });
        createTestTransportation(ankaraLocation, izmirLocation, TransportationType.FLIGHT,
                new Integer[] { 1, 2, 3, 4, 5 });

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<TransportationDTO> result = transportationService.findAllWithFilters(
                pageable, istanbulLocation.getId(), null, TransportationType.FLIGHT);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOriginLocationId()).isEqualTo(istanbulLocation.getId());
        assertThat(result.getContent().get(0).getTransportationType()).isEqualTo(TransportationType.FLIGHT);
    }

    @Test
    @DisplayName("Should find all transportations without filters")
    void shouldFindAllTransportationsWithoutFilters() {
        // Given
        createTestTransportation(istanbulLocation, ankaraLocation, TransportationType.FLIGHT,
                new Integer[] { 1, 2, 3, 4, 5 });
        createTestTransportation(istanbulLocation, izmirLocation, TransportationType.BUS,
                new Integer[] { 1, 2, 3, 4, 5, 6, 7 });

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<TransportationDTO> result = transportationService.findAllWithFilters(pageable, null, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Should find transportation by id")
    void shouldFindTransportationById() {
        // Given
        Transportation savedTransportation = createTestTransportation(
                istanbulLocation, ankaraLocation, TransportationType.FLIGHT, new Integer[] { 1, 2, 3, 4, 5 });

        // When
        TransportationDTO result = transportationService.findById(savedTransportation.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedTransportation.getId());
        assertThat(result.getOriginLocationId()).isEqualTo(istanbulLocation.getId());
        assertThat(result.getDestinationLocationId()).isEqualTo(ankaraLocation.getId());
        assertThat(result.getTransportationType()).isEqualTo(TransportationType.FLIGHT);
        assertThat(result.getOperatingDays()).containsExactly(1, 2, 3, 4, 5);
        assertThat(result.getOriginLocation()).isNotNull();
        assertThat(result.getDestinationLocation()).isNotNull();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when transportation not found by id")
    void shouldThrowResourceNotFoundExceptionWhenTransportationNotFoundById() {
        // When & Then
        assertThatThrownBy(() -> transportationService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Transportation not found with id: 999");
    }

    @Test
    @DisplayName("Should create new transportation successfully")
    void shouldCreateNewTransportationSuccessfully() {
        // Given
        TransportationDTO transportationDTO = TransportationDTO.builder()
                .originLocationId(istanbulLocation.getId())
                .destinationLocationId(ankaraLocation.getId())
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(new Integer[] { 1, 2, 3, 4, 5 })
                .build();

        // When
        TransportationDTO result = transportationService.create(transportationDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getOriginLocationId()).isEqualTo(istanbulLocation.getId());
        assertThat(result.getDestinationLocationId()).isEqualTo(ankaraLocation.getId());
        assertThat(result.getTransportationType()).isEqualTo(TransportationType.FLIGHT);
        assertThat(result.getOperatingDays()).containsExactly(1, 2, 3, 4, 5);
        assertThat(result.getOriginLocation()).isNotNull();
        assertThat(result.getDestinationLocation()).isNotNull();

        // Verify in database
        Transportation savedTransportation = transportationRepository.findById(result.getId()).orElse(null);
        assertThat(savedTransportation).isNotNull();
        assertThat(savedTransportation.getTransportationType()).isEqualTo(TransportationType.FLIGHT);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating transportation with non-existent origin location")
    void shouldThrowResourceNotFoundExceptionWhenCreatingTransportationWithNonExistentOriginLocation() {
        // Given
        TransportationDTO transportationDTO = TransportationDTO.builder()
                .originLocationId(999L) // Non-existent
                .destinationLocationId(ankaraLocation.getId())
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(new Integer[] { 1, 2, 3, 4, 5 })
                .build();

        // When & Then
        assertThatThrownBy(() -> transportationService.create(transportationDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Origin location not found with id: 999");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating transportation with non-existent destination location")
    void shouldThrowResourceNotFoundExceptionWhenCreatingTransportationWithNonExistentDestinationLocation() {
        // Given
        TransportationDTO transportationDTO = TransportationDTO.builder()
                .originLocationId(istanbulLocation.getId())
                .destinationLocationId(999L) // Non-existent
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(new Integer[] { 1, 2, 3, 4, 5 })
                .build();

        // When & Then
        assertThatThrownBy(() -> transportationService.create(transportationDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Destination location not found with id: 999");
    }

    @Test
    @DisplayName("Should update transportation successfully")
    void shouldUpdateTransportationSuccessfully() {
        // Given
        Transportation savedTransportation = createTestTransportation(
                istanbulLocation, ankaraLocation, TransportationType.FLIGHT, new Integer[] { 1, 2, 3, 4, 5 });

        TransportationDTO updateDTO = TransportationDTO.builder()
                .originLocationId(istanbulLocation.getId())
                .destinationLocationId(izmirLocation.getId()) // Changed destination
                .transportationType(TransportationType.BUS) // Changed type
                .operatingDays(new Integer[] { 1, 2, 3, 4, 5, 6, 7 }) // Changed operating days
                .build();

        // When
        TransportationDTO result = transportationService.update(savedTransportation.getId(), updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedTransportation.getId());
        assertThat(result.getOriginLocationId()).isEqualTo(istanbulLocation.getId());
        assertThat(result.getDestinationLocationId()).isEqualTo(izmirLocation.getId());
        assertThat(result.getTransportationType()).isEqualTo(TransportationType.BUS);
        assertThat(result.getOperatingDays()).containsExactly(1, 2, 3, 4, 5, 6, 7);

        // Verify in database
        Transportation updatedTransportation = transportationRepository.findById(savedTransportation.getId())
                .orElse(null);
        assertThat(updatedTransportation).isNotNull();
        assertThat(updatedTransportation.getTransportationType()).isEqualTo(TransportationType.BUS);
        assertThat(updatedTransportation.getDestinationLocation().getId()).isEqualTo(izmirLocation.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent transportation")
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentTransportation() {
        // Given
        TransportationDTO updateDTO = TransportationDTO.builder()
                .originLocationId(istanbulLocation.getId())
                .destinationLocationId(ankaraLocation.getId())
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(new Integer[] { 1, 2, 3, 4, 5 })
                .build();

        // When & Then
        assertThatThrownBy(() -> transportationService.update(999L, updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Transportation not found with id: 999");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating transportation with non-existent origin location")
    void shouldThrowResourceNotFoundExceptionWhenUpdatingTransportationWithNonExistentOriginLocation() {
        // Given
        Transportation savedTransportation = createTestTransportation(
                istanbulLocation, ankaraLocation, TransportationType.FLIGHT, new Integer[] { 1, 2, 3, 4, 5 });

        TransportationDTO updateDTO = TransportationDTO.builder()
                .originLocationId(999L) // Non-existent
                .destinationLocationId(ankaraLocation.getId())
                .transportationType(TransportationType.FLIGHT)
                .operatingDays(new Integer[] { 1, 2, 3, 4, 5 })
                .build();

        // When & Then
        assertThatThrownBy(() -> transportationService.update(savedTransportation.getId(), updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Origin location not found with id: 999");
    }

    @Test
    @DisplayName("Should soft delete transportation successfully")
    void shouldSoftDeleteTransportationSuccessfully() {
        // Given
        Transportation savedTransportation = createTestTransportation(
                istanbulLocation, ankaraLocation, TransportationType.FLIGHT, new Integer[] { 1, 2, 3, 4, 5 });

        // When
        transportationService.delete(savedTransportation.getId());
        em.flush();
        em.clear();
        // Then
        // Verify transportation is soft deleted (not found in normal queries)
        assertThatThrownBy(() -> transportationService.findById(savedTransportation.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Transportation not found with id: " + savedTransportation.getId());
        em.flush();
        em.clear();
        // Verify transportation still exists in database but with deleted_at set
        Transportation deletedTransportation = transportationRepository.findById(savedTransportation.getId())
                .orElse(null);
        assertThat(deletedTransportation).isNull(); // Due to @SQLRestriction
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent transportation")
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentTransportation() {
        // When & Then
        assertThatThrownBy(() -> transportationService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Transportation not found with id: 999");
    }

    @Test
    @DisplayName("Should find transportations by origin and destination")
    void shouldFindTransportationsByOriginAndDestination() {
        // Given
        createTestTransportation(istanbulLocation, ankaraLocation, TransportationType.FLIGHT,
                new Integer[] { 1, 2, 3, 4, 5 });
        createTestTransportation(istanbulLocation, izmirLocation, TransportationType.BUS,
                new Integer[] { 1, 2, 3, 4, 5, 6, 7 });
        createTestTransportation(ankaraLocation, izmirLocation, TransportationType.FLIGHT,
                new Integer[] { 1, 2, 3, 4, 5 });

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<TransportationDTO> result = transportationService.findAllWithFilters(
                pageable, istanbulLocation.getId(), ankaraLocation.getId(), null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOriginLocationId()).isEqualTo(istanbulLocation.getId());
        assertThat(result.getContent().get(0).getDestinationLocationId()).isEqualTo(ankaraLocation.getId());
    }

    @Test
    @DisplayName("Should find transportations by transportation type only")
    void shouldFindTransportationsByTransportationTypeOnly() {
        // Given
        createTestTransportation(istanbulLocation, ankaraLocation, TransportationType.FLIGHT,
                new Integer[] { 1, 2, 3, 4, 5 });
        createTestTransportation(istanbulLocation, izmirLocation, TransportationType.BUS,
                new Integer[] { 1, 2, 3, 4, 5, 6, 7 });
        createTestTransportation(ankaraLocation, izmirLocation, TransportationType.FLIGHT,
                new Integer[] { 1, 2, 3, 4, 5 });

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<TransportationDTO> result = transportationService.findAllWithFilters(
                pageable, null, null, TransportationType.BUS);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTransportationType()).isEqualTo(TransportationType.BUS);
    }

    private Location createTestLocation(String name, String country, String city, String locationCode) {
        Location location = Location.builder()
                .name(name)
                .country(country)
                .city(city)
                .locationCode(locationCode)
                .build();
        return locationRepository.save(location);
    }

    private Transportation createTestTransportation(Location origin, Location destination,
            TransportationType type, Integer[] operatingDays) {
        Transportation transportation = Transportation.builder()
                .originLocation(origin)
                .destinationLocation(destination)
                .transportationType(type)
                .operatingDays(operatingDays)
                .build();
        return transportationRepository.save(transportation);
    }
}
