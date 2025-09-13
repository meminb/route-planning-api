package com.turkishairlines.routeplanning.repository;

import com.turkishairlines.routeplanning.model.entity.Location;
import com.turkishairlines.routeplanning.model.entity.Transportation;
import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransportationJpaRepository extends JpaRepository<Transportation, Long> {

    List<Transportation> findByOriginLocation(Location originLocation);

    List<Transportation> findByDestinationLocation(Location destinationLocation);

    List<Transportation> findByOriginLocationAndDestinationLocation(Location originLocation,
            Location destinationLocation);

    List<Transportation> findByTransportationType(TransportationType transportationType);

    @Query("SELECT t FROM Transportation t WHERE t.originLocation = :origin AND t.transportationType = :type")
    List<Transportation> findByOriginAndType(@Param("origin") Location origin, @Param("type") TransportationType type);

    @Query("SELECT t FROM Transportation t WHERE t.destinationLocation = :destination AND t.transportationType = :type")
    List<Transportation> findByDestinationAndType(@Param("destination") Location destination,
            @Param("type") TransportationType type);

    @Query("SELECT t FROM Transportation t WHERE t.originLocation.id = :originId")
    List<Transportation> findByOriginLocationId(@Param("originId") Long originId);

    @Query("SELECT t FROM Transportation t WHERE t.destinationLocation.id = :destinationId")
    List<Transportation> findByDestinationLocationId(@Param("destinationId") Long destinationId);
}
