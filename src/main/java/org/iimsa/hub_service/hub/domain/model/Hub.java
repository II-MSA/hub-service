package org.iimsa.hub_service.hub.domain.model;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;
import org.iimsa.common.domain.BaseEntity;
import org.iimsa.hub_service.hub.domain.event.HubEvents;
import org.iimsa.hub_service.hub.domain.exception.MasterOnlyException;
import org.iimsa.hub_service.hub.domain.service.AddressResolver;
import org.iimsa.hub_service.hub.domain.service.CompanyProvider;
import org.iimsa.hub_service.hub.domain.service.RoleCheck;

@Getter
@Entity @ToString
@Table(name="P_HUB")
@Access(AccessType.FIELD)
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hub extends BaseEntity {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.UUID)
    private HubId id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(name = "address")
    private Address address;

    @Embedded
    private HubManager hubManager;

    @OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "hub_id")
    private List<HubProduct> products = new ArrayList<>();

    @Builder
    public Hub(String name, String address, AddressResolver addressResolver,
               UUID hubManagerId, String hubManagerName,
               RoleCheck roleCheck, HubEvents events) {

        checkAuthority(roleCheck);

        this.id = HubId.of();
        this.name = name; // hub name
        this.address = new Address(address, addressResolver);

        if (hubManagerId != null && hubManagerName != null) {
            this.hubManager = new HubManager(hubManagerId, hubManagerName);
        }

        events.hubChanged(this, true);
    }

    public void addProduct(int stock, UUID companyId, CompanyProvider storeProvider, RoleCheck roleCheck) {

        checkAuthority(roleCheck);

        HubProduct item = HubProduct.builder()
                .stock(stock)
                .companyId(companyId)
                .companyProvider(storeProvider)
                .build();

        this.products.add(item);
    }

    public void removeProduct(ProductId productId, RoleCheck roleCheck) {
        checkAuthority(roleCheck);

        this.products.stream()
                .filter(item -> item.getId().equals(productId))
                .findFirst()
                .ifPresent(HubProduct::delete);
    }


    public void update(String name, String address, Double latitude, Double longitude) {
        if (name != null) this.name = name;

    }

    // change hub name
    public void changeName(String name, RoleCheck roleCheck, HubEvents events) {
        checkAuthority(roleCheck);

        if (this.name.equals(name)) return;

        this.name = name;

        events.hubChanged(this, false);
    }

    // change Address
    public void changeAddress(String address, AddressResolver resolver, RoleCheck roleCheck, HubEvents events) {
        checkAuthority(roleCheck);

        if (this.address.getAddress() != null && this.address.getAddress().equals(address)) {
            return;
        }

        this.address = new Address(address, resolver);

        events.hubChanged(this, true);
    }

    // change hubManager
    public void changeHubManager(UUID hubManagerId, String hubManagerName, RoleCheck roleCheck, HubEvents events) {
        checkAuthority(roleCheck);
        this.hubManager = new HubManager(hubManagerId, hubManagerName);
        events.hubChanged(this, true);
    }

    // hub delete (Not hub Product)
    public void delete(RoleCheck roleCheck, HubEvents events) {
        checkAuthority(roleCheck);

        if (this.deletedAt != null) return;

        super.delete(null);

        events.hubChanged(this, true);
    }

    private void checkAuthority(RoleCheck roleCheck) {
        if (!roleCheck.hasRole(UserType.MASTER)) {
            throw new MasterOnlyException();
        }
    }
}
