package org.iimsa.hub_service.hub.domain.service;

import java.util.List;
import org.iimsa.hub_service.hub.domain.model.UserType;

public interface RoleCheck {
    boolean hasRole(UserType type);
    boolean hasRole(List<UserType> types);
}
