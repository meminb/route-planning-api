package com.turkishairlines.routeplanning.model.entity;

import com.turkishairlines.routeplanning.model.enumaration.TransportationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "transportations")
@SQLDelete(sql = "UPDATE transportations SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class Transportation extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transportations_origin"))
    private Location originLocation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transportations_destination"))
    private Location destinationLocation;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transportation_type", nullable = false, length = 16)
    private TransportationType transportationType;

    @Column(name = "operating_days", columnDefinition = "integer[]")
    private Integer[] operatingDays;
}
