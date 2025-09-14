package com.turkishairlines.routeplanning.repository;

import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TransportationRepository extends JpaRepository<Transportation, Long> {

        List<Transportation> findByOriginLocationAndTransportationTypeNot(
                        Location origin, TransportationType type);

        List<Transportation> findByDestinationLocationAndTransportationTypeNot(
                        Location destination, TransportationType type);

        List<Transportation> findByTransportationTypeAndOriginLocationInAndDestinationLocationIn(
                        TransportationType type,
                        Collection<Location> origins,
                        Collection<Location> destinations);

        List<Transportation> findByOriginLocation(Location originLocation);

        List<Transportation> findByDestinationLocation(Location destinationLocation);

        List<Transportation> findByOriginLocationAndDestinationLocation(Location originLocation,
                        Location destinationLocation);

        @Query("SELECT t FROM Transportation t " +
                        "INNER JOIN t.originLocation ol " +
                        "INNER JOIN t.destinationLocation dl " +
                        "WHERE (:originLocationId IS NULL OR ol.id = :originLocationId) " +
                        "AND (:destinationLocationId IS NULL OR dl.id = :destinationLocationId) " +
                        "AND (:transportationType IS NULL OR t.transportationType = :transportationType) " +
                        "AND t.deletedAt IS NULL " +
                        "ORDER BY t.createdAt DESC")
        Page<Transportation> findAllWithFilters(@Param("originLocationId") Long originLocationId,
                        @Param("destinationLocationId") Long destinationLocationId,
                        @Param("transportationType") TransportationType transportationType,
                        Pageable pageable);
}
