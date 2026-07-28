package org.iimsa.hub_service.hub.domain.service.dto;

import java.util.List;
import java.util.UUID;
import org.iimsa.hub_service.hub.domain.model.HubRoutePath;

public record HubRoutePathData(
        UUID originHubId,
        UUID destinationHubId,
        Integer totalDuration,
        Double totalDistance,
        List<SegmentResponse> segments
) {
    public static HubRoutePathData from(HubRoutePath path) {
        // 1. 도메인 Segment들을 DTO SegmentResponse로 변환
        List<SegmentResponse> segmentResponses = path.segments().stream()
                .map(SegmentResponse::from)
                .toList();

        // 2. 최종 DTO 반환
        return new HubRoutePathData(
                path.originHubId(),
                path.destinationHubId(),
                path.totalDuration(),
                path.totalDistance(),
                segmentResponses
        );
    }

    // 내부에서 사용할 Segment용 DTO
    public record SegmentResponse(
            int sequence,
            UUID fromHubId,
            String fromHubName,
            UUID toHubId,
            String toHubName,
            Integer estimatedDuration,
            Double estimatedDistance
    ) {
        // 도메인 Segment -> DTO SegmentResponse 변환 메서드
        public static SegmentResponse from(HubRoutePath.Segment segment) {
            return new SegmentResponse(
                    segment.sequence(),
                    segment.fromHubId(),
                    segment.fromHubName(),
                    segment.toHubId(),
                    segment.toHubName(),
                    segment.estimatedDuration(),
                    segment.estimatedDistance()
            );
        }
    }
}
