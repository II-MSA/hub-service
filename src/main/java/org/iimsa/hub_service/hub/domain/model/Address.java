package org.iimsa.hub_service.hub.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.iimsa.common.exception.BadRequestException;
import org.iimsa.hub_service.hub.domain.service.AddressResolver;
import org.iimsa.hub_service.hub.domain.service.Coordinates;
import org.springframework.util.StringUtils;

@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {
    @Column(name = "address_detail", columnDefinition = "TEXT", nullable = false)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    protected Address(String address, AddressResolver resolver) {
        if (!StringUtils.hasText(address)) {
            throw new BadRequestException("주소는 필수입력 값 입니다.");
        }

        Coordinates coordinates = resolver.resolve(address);
        if (coordinates == null) {
            throw new BadRequestException("유효하지 않은 주소입니다: " + address);
        }

        this.address = address;
        this.latitude = coordinates.latitude();
        this.longitude = coordinates.longitude();
    }
}
