package org.iimsa.hub_service.hub.domain.event.payload;

public enum HubAction {
    CREATED,           // 허브 생성
    LOCATION_UPDATED,  // 위치 수정
    INFO_UPDATED,      // 이름 등 단순 정보 수정
    DELETED            // 허브 삭제요
}
