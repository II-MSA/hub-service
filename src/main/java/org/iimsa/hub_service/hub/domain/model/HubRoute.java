package org.iimsa.hub_service.hub.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.iimsa.common.domain.BaseEntity;

@Getter
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

    @Builder
    public HubRoute(UUID fromHubId, String fromHubName, UUID toHubId, String toHubName, Double estimatedDistance, Integer estimatedDuration) {
        this.fromHubId = fromHubId;
        this.fromHubName = fromHubName;
        this.toHubId = toHubId;
        this.toHubName = toHubName;
        this.estimatedDistance = estimatedDistance;
        this.estimatedDuration = estimatedDuration;
    }

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
