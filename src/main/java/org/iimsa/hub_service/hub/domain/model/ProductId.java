package org.iimsa.hub_service.hub.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Embeddable
public record ProductId(
        @JdbcTypeCode(SqlTypes.UUID)
        @Column(length=36, name="product_id")
        UUID id
) implements Serializable {
    public static ProductId of(UUID id) {
        return new ProductId(id);
    }

    public static ProductId of() {
        return new ProductId(UUID.randomUUID());
    }

    public static ProductId fromString(String id) {
        return new ProductId(UUID.fromString(id));
    }
}
