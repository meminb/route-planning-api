package com.turkishairlines.routeplanning.service;

import com.turkishairlines.routeplanning.exception.DuplicateResourceException;
import com.turkishairlines.routeplanning.exception.ResourceNotFoundException;
import com.turkishairlines.routeplanning.model.dto.LocationDTO;
import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.repository.LocationRepository;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class LocationServiceIntegrationTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationRepository locationRepository;

    @BeforeEach
    void setUp() {
        locationRepository.deleteAll();
    }

    @Test
    @DisplayName("Should find all locations with pagination")
    void shouldFindAllLocationsWithPagination() {
        // Given
        createTestLocation("Istanbul Airport", "Turkey", "Istanbul", "IST");
        createTestLocation("Ankara Airport", "Turkey", "Ankara", "ESB");
        createTestLocation("Izmir Airport", "Turkey", "Izmir", "ADB");

        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<LocationDTO> result = locationService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find location by id")
    void shouldFindLocationById() {
        // Given
        Location savedLocation = createTestLocation("Istanbul Airport", "Turkey", "Istanbul", "IST");

        // When
        LocationDTO result = locationService.findById(savedLocation.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedLocation.getId());
        assertThat(result.getName()).isEqualTo("Istanbul Airport");
        assertThat(result.getCountry()).isEqualTo("Turkey");
        assertThat(result.getCity()).isEqualTo("Istanbul");
        assertThat(result.getLocationCode()).isEqualTo("IST");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when location not found by id")
    void shouldThrowResourceNotFoundExceptionWhenLocationNotFoundById() {
        // When & Then
        assertThatThrownBy(() -> locationService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Location not found with id: 999");
    }

    @Test
    @DisplayName("Should find location by location code")
    void shouldFindLocationByLocationCode() {
        // Given
        createTestLocation("Istanbul Airport", "Turkey", "Istanbul", "IST");

        // When
        LocationDTO result = locationService.findByLocationCode("IST");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Istanbul Airport");
        assertThat(result.getLocationCode()).isEqualTo("IST");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when location not found by code")
    void shouldThrowResourceNotFoundExceptionWhenLocationNotFoundByCode() {
        // When & Then
        assertThatThrownBy(() -> locationService.findByLocationCode("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Location not found with code: INVALID");
    }

    @Test
    @DisplayName("Should create new location successfully")
    void shouldCreateNewLocationSuccessfully() {
        // Given
        LocationDTO locationDTO = LocationDTO.builder()
                .name("Istanbul Airport")
                .country("Turkey")
                .city("Istanbul")
                .locationCode("IST")
                .build();

        // When
        LocationDTO result = locationService.create(locationDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Istanbul Airport");
        assertThat(result.getCountry()).isEqualTo("Turkey");
        assertThat(result.getCity()).isEqualTo("Istanbul");
        assertThat(result.getLocationCode()).isEqualTo("IST");

        // Verify in database
        Location savedLocation = locationRepository.findById(result.getId()).orElse(null);
        assertThat(savedLocation).isNotNull();
        assertThat(savedLocation.getName()).isEqualTo("Istanbul Airport");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when creating location with existing code")
    void shouldThrowDuplicateResourceExceptionWhenCreatingLocationWithExistingCode() {
        // Given
        createTestLocation("Istanbul Airport", "Turkey", "Istanbul", "IST");

        LocationDTO duplicateLocationDTO = LocationDTO.builder()
                .name("Another Airport")
                .country("Turkey")
                .city("Istanbul")
                .locationCode("IST") // Same code
                .build();

        // When & Then
        assertThatThrownBy(() -> locationService.create(duplicateLocationDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Location with code 'IST' already exists");
    }

    @Test
    @DisplayName("Should update location successfully")
    void shouldUpdateLocationSuccessfully() {
        // Given
        Location savedLocation = createTestLocation("Istanbul Airport", "Turkey", "Istanbul", "IST");

        LocationDTO updateDTO = LocationDTO.builder()
                .name("Istanbul New Airport")
                .country("Turkey")
                .city("Istanbul")
                .locationCode("IST")
                .build();

        // When
        LocationDTO result = locationService.update(savedLocation.getId(), updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedLocation.getId());
        assertThat(result.getName()).isEqualTo("Istanbul New Airport");
        assertThat(result.getLocationCode()).isEqualTo("IST");

        // Verify in database
        Location updatedLocation = locationRepository.findById(savedLocation.getId()).orElse(null);
        assertThat(updatedLocation).isNotNull();
        assertThat(updatedLocation.getName()).isEqualTo("Istanbul New Airport");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when updating location with existing code")
    void shouldThrowDuplicateResourceExceptionWhenUpdatingLocationWithExistingCode() {
        // Given
        Location location1 = createTestLocation("Istanbul Airport", "Turkey", "Istanbul", "IST");
        createTestLocation("Ankara Airport", "Turkey", "Ankara", "ESB");

        LocationDTO updateDTO = LocationDTO.builder()
                .name("Istanbul Airport")
                .country("Turkey")
                .city("Istanbul")
                .locationCode("ESB") // Same as location2
                .build();

        // When & Then
        assertThatThrownBy(() -> locationService.update(location1.getId(), updateDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Location with code 'ESB' already exists");
    }

    @Test
    @DisplayName("Should not throw exception when updating location with same code")
    void shouldNotThrowExceptionWhenUpdatingLocationWithSameCode() {
        // Given
        Location savedLocation = createTestLocation("Istanbul Airport", "Turkey", "Istanbul", "IST");

        LocationDTO updateDTO = LocationDTO.builder()
                .name("Istanbul New Airport")
                .country("Turkey")
                .city("Istanbul")
                .locationCode("IST") // Same code as original
                .build();

        // When
        LocationDTO result = locationService.update(savedLocation.getId(), updateDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Istanbul New Airport");
        assertThat(result.getLocationCode()).isEqualTo("IST");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent location")
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentLocation() {
        // Given
        LocationDTO updateDTO = LocationDTO.builder()
                .name("Istanbul Airport")
                .country("Turkey")
                .city("Istanbul")
                .locationCode("IST")
                .build();

        // When & Then
        assertThatThrownBy(() -> locationService.update(999L, updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Location not found with id: 999");
    }

    @Test
    @DisplayName("Should soft delete location successfully")
    void shouldSoftDeleteLocationSuccessfully() {
        // Given
        Location savedLocation = createTestLocation("Istanbul Airport", "Turkey", "Istanbul", "IST");

        // When
        locationService.delete(savedLocation.getId());
        em.flush();
        em.clear();

        assertThatThrownBy(() -> locationService.findById(savedLocation.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Location not found with id: " + savedLocation.getId());

        em.flush();
        em.clear();
        Location deletedLocation = locationRepository.findById(savedLocation.getId()).orElse(null);
        assertThat(deletedLocation).isNull(); // Due to @SQLRestriction
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent location")
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentLocation() {
        // When & Then
        assertThatThrownBy(() -> locationService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Location not found with id: 999");
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
}
