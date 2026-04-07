package org.iimsa.hub_service.hub.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.iimsa.common.exception.BadRequestException;
import org.iimsa.hub_service.hub.domain.exception.CompanyNotFoundException;
import org.iimsa.hub_service.hub.domain.service.CompanyProvider;
import org.iimsa.hub_service.hub.domain.service.dto.CompanyData;

@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company {
    @Column(length=36, name = "company_id", nullable = false)
    private UUID id;

    @Column(length=100, name="company_name", nullable = false)
    private String name;

    protected Company(UUID id, CompanyProvider provider) {
        if (id == null) {
            throw new BadRequestException("업체 ID는 필수 입력값입니다.");
        }

        if (provider == null) {
            throw new BadRequestException("업체 정보 조회를 위한 Provider가 제공되지 않았습니다.");
        }

        CompanyData data = provider.getCompany(id);
        if (data == null || data.id() == null) {
            throw new CompanyNotFoundException(id);
        }

        this.id = data.id();
        this.name = data.name();
    }
}
