package org.iimsa.hub_service.hub.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record HubRoutePath(

        UUID originHubId,
        UUID destinationHubId,

        int totalDuration,
        double totalDistance,

        List<Segment> segments

) {

    public static HubRoutePath of(UUID originHubId, UUID destinationHubId, List<HubRoute> orderedRoutes) {
        List<Segment> segments = new ArrayList<>();
        int totalDuration = 0;
        double totalDistance = 0.0;

        for (int i = 0; i < orderedRoutes.size(); i++) {
            HubRoute route = orderedRoutes.get(i);
            segments.add(new Segment(
                    i + 1,
                    route.getFromHubId(),
                    route.getFromHubName(),
                    route.getToHubId(),
                    route.getToHubName(),
                    route.getEstimatedDuration(),
                    route.getEstimatedDistance()
            ));
            if (route.getEstimatedDuration() != null) totalDuration += route.getEstimatedDuration();
            if (route.getEstimatedDistance() != null) totalDistance += route.getEstimatedDistance();
        }

        return new HubRoutePath(originHubId, destinationHubId, totalDuration, totalDistance, segments);
    }

    public record Segment(
            int sequence,
            UUID fromHubId,
            String fromHubName,
            UUID toHubId,
            String toHubName,
            Integer estimatedDuration,  // 분
            Double estimatedDistance    // km
    ) {
    }
}
