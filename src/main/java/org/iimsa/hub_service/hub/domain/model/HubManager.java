package org.iimsa.hub_service.hub.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class HubManager {
    @Column(length = 36, name = "hub_manager_id", nullable = false)
    private UUID hubManagerId;

    @Column(name = "hub_manager_name", length = 50, nullable = false)
    private String hubManagerName;

}
