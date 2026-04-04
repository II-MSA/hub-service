package org.iimsa.hub_service.hub.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.iimsa.common.domain.BaseEntity;

import java.util.UUID;

@Entity
@Getter
@Builder
@Access(AccessType.FIELD)
@Table(name = "p_hub")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Hub extends BaseEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 255)
    private String address;

    private Double latitude;

    private Double longitude;

    // ──────────────────────────────────────────────
    // 비즈니스 메서드
    // ──────────────────────────────────────────────

    public void update(String name, String address, Double latitude, Double longitude) {
        if (name != null) this.name = name;
        if (address != null) this.address = address;
        if (latitude != null) this.latitude = latitude;
        if (longitude != null) this.longitude = longitude;
    }

    public void softDelete(String deletedBy) {
        super.delete(deletedBy);
    }
}
