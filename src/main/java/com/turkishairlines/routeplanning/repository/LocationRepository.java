package com.turkishairlines.routeplanning.repository;

import com.turkishairlines.routeplanning.model.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByLocationCode(String locationCode);

    boolean existsByLocationCode(String locationCode);

    @Query("SELECT l FROM Location l WHERE l.locationCode = :locationCode AND l.deletedAt IS NULL")
    Optional<Location> findActiveByLocationCode(@Param("locationCode") String locationCode);
}
