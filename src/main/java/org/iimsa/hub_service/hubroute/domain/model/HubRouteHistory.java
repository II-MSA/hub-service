package org.iimsa.hub_service.hubroute.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.iimsa.common.domain.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 허브 경로 실제 소요시간 이력
 * DeliveryRoute ARRIVED 시점에 기록 → DB_AVERAGE fallback 계산에 활용
 */
@Entity
@Getter
@Builder
@Access(AccessType.FIELD)
@Table(
        name = "p_hub_route_history",
        indexes = {
                @Index(name = "idx_hub_route_history_from_to", columnList = "from_hub_id, to_hub_id"),
                @Index(name = "idx_hub_route_history_recorded_at", columnList = "recorded_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HubRouteHistory extends BaseEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "from_hub_id", length = 36, nullable = false)
    private UUID fromHubId;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "to_hub_id", length = 36, nullable = false)
    private UUID toHubId;

    /** 실제 소요 시간 (분) */
    @Column(name = "actual_duration", nullable = false)
    private Integer actualDuration;

    /** 이력 기록 시각 (DeliveryRoute ARRIVED 시각) */
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
