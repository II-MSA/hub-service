package org.iimsa.hub_service.hubroute.domain.model;

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
@Table(
        name = "p_hub_route",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_hub_route_from_to",
                columnNames = {"from_hub_id", "to_hub_id"}
        ),
        indexes = {
                @Index(name = "idx_hub_route_from_hub_id", columnList = "from_hub_id"),
                @Index(name = "idx_hub_route_to_hub_id", columnList = "to_hub_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HubRoute extends BaseEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "from_hub_id", length = 36, nullable = false)
    private UUID fromHubId;

    @Column(name = "from_hub_name", length = 100, nullable = false)
    private String fromHubName;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "to_hub_id", length = 36, nullable = false)
    private UUID toHubId;

    @Column(name = "to_hub_name", length = 100, nullable = false)
    private String toHubName;

    @Column(name = "estimated_distance")
    private Double estimatedDistance; // 예상 거리 (km)

    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // 예상 소요 시간 (분)

    // ──────────────────────────────────────────────
    // 비즈니스 메서드
    // ──────────────────────────────────────────────

    public void update(Double estimatedDistance, Integer estimatedDuration) {
        if (estimatedDistance != null) this.estimatedDistance = estimatedDistance;
        if (estimatedDuration != null) this.estimatedDuration = estimatedDuration;
    }

    public void updateHubName(String fromHubName, String toHubName) {
        if (fromHubName != null) this.fromHubName = fromHubName;
        if (toHubName != null) this.toHubName = toHubName;
    }

    public void softDelete(String deletedBy) {
        super.delete(deletedBy);
    }
}
