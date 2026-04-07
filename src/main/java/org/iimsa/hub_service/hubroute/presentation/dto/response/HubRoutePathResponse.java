package org.iimsa.hub_service.hubroute.presentation.dto.response;

import org.iimsa.hub_service.hubroute.domain.model.HubRoutePath;

import java.util.List;
import java.util.UUID;

public record HubRoutePathResponse(

        UUID originHubId,
        UUID destinationHubId,
        int totalDuration,      // 전체 예상 소요시간 합산 (분)
        double totalDistance,   // 전체 예상 거리 합산 (km)
        List<SegmentResponse> segments

) {
    public static HubRoutePathResponse from(HubRoutePath path) {
        List<SegmentResponse> segments = path.segments().stream()
                .map(SegmentResponse::from)
                .toList();

        return new HubRoutePathResponse(
                path.originHubId(),
                path.destinationHubId(),
                path.totalDuration(),
                path.totalDistance(),
                segments
        );
    }

    public record SegmentResponse(
            int sequence,
            UUID fromHubId,
            String fromHubName,
            UUID toHubId,
            String toHubName,
            Integer estimatedDuration,
            Double estimatedDistance
    ) {
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
