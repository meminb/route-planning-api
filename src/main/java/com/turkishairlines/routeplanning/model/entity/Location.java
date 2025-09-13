package com.turkishairlines.routeplanning.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "locations", uniqueConstraints = @UniqueConstraint(name = "uk_locations_location_code", columnNames = "location_code"))
@SQLDelete(sql = "UPDATE locations SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldNameConstants
public class Location extends BaseEntity {

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String country;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String city;

    @NotBlank
    @Size(max = 16)
    @Column(name = "location_code", nullable = false, length = 16)
    private String locationCode;
}
