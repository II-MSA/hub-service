package org.iimsa.hub_service.hubroute.domain.repository;

import org.iimsa.hub_service.hubroute.domain.cache.LiveRouteCache;
import org.iimsa.hub_service.hubroute.domain.cache.RouteSnapshotCache;

import java.util.Optional;
import java.util.UUID;

public interface HubRouteCacheRepository {

    // ── Live 캐시 ──────────────────────────────────

    Optional<LiveRouteCache> getLive(UUID fromHubId, UUID toHubId);

    void setLive(UUID fromHubId, UUID toHubId, LiveRouteCache cache);

    // ── 배차 스냅샷 ────────────────────────────────

    Optional<RouteSnapshotCache> getSnapshot(String snapshotId, UUID fromHubId, UUID toHubId);

    void setSnapshot(String snapshotId, UUID fromHubId, UUID toHubId, RouteSnapshotCache cache);

    // ── 최신 스냅샷 ID ─────────────────────────────

    Optional<String> getLatestSnapshotId();

    void setLatestSnapshotId(String snapshotId);
}
