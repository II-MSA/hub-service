package org.iimsa.hub_service.hub.infrastructure.persistence;

import static org.iimsa.hub_service.hub.domain.model.QHub.hub;
import static org.iimsa.hub_service.hub.domain.model.QHubProduct.hubProduct;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.iimsa.hub_service.hub.domain.model.Hub;
import org.iimsa.hub_service.hub.domain.model.HubId;
import org.iimsa.hub_service.hub.domain.query.HubQueryDto;
import org.iimsa.hub_service.hub.domain.repository.HubQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class HubQueryRepositoryImpl implements HubQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final JpaHubRepository jpaHubRepository;

    @Override
    public Optional<Hub> findById(HubId hubId) {
        return jpaHubRepository.findById(hubId);
    }

    @Override
    public Page<Hub> search(HubQueryDto.Search searchDto, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        // 허브 자체는 삭제되지 않아야
        builder.and(hub.deletedAt.isNull());

        // 1. Hub 기본 검색 조건
        if (StringUtils.hasText(searchDto.getHubName())) {
            builder.and(hub.name.contains(searchDto.getHubName()));
        }
        if (searchDto.getHubManagerId() != null) {
            builder.and(hub.hubManager.hubManagerId.eq(searchDto.getHubManagerId()));
        }
        if (StringUtils.hasText(searchDto.getHubManagerName())) {
            builder.and(hub.hubManager.hubManagerName.contains(searchDto.getHubManagerName()));
        }

        // 2. Company 연관 검색 조건
        boolean needJoin = false;
        if (StringUtils.hasText(searchDto.getCompanyName())) {
            builder.and(hubProduct.company.name.contains(searchDto.getCompanyName()));
            needJoin = true;
        }
        if (searchDto.getCompanyIds() != null && !searchDto.getCompanyIds().isEmpty()) {
            builder.and(hubProduct.company.id.in(searchDto.getCompanyIds()));
            needJoin = true;
        }

        if (needJoin) {
            builder.and(hubProduct.deletedAt.isNull());
        }

        // 3. 데이터 조회 쿼리
        JPAQuery<Hub> query = queryFactory.selectFrom(hub);

        if (needJoin) {
            query.leftJoin(hub.products, hubProduct).distinct();
        }

        List<Hub> content = query
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 4. Count 쿼리
        JPAQuery<Long> countQuery = queryFactory.select(hub.countDistinct()).from(hub);
        if (needJoin) {
            countQuery.leftJoin(hub.products, hubProduct);
        }
        countQuery.where(builder);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Hub> findAllByHubManagerId(UUID hubManagerId, Pageable pageable) {
        List<Hub> content = queryFactory
                .selectFrom(hub)
                .where(
                        hub.hubManager.hubManagerId.eq(hubManagerId),
                        hub.deletedAt.isNull()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(hub.count())
                .from(hub)
                .where(
                        hub.hubManager.hubManagerId.eq(hubManagerId),
                        hub.deletedAt.isNull()
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Hub> findAllByCompanyId(UUID companyId, Pageable pageable) {
        List<Hub> content = queryFactory
                .selectFrom(hub)
                .leftJoin(hub.products, hubProduct)
                .where(
                        hubProduct.company.id.eq(companyId),
                        hub.deletedAt.isNull(),
                        hubProduct.deletedAt.isNull()
                )
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(hub.countDistinct())
                .from(hub)
                .leftJoin(hub.products, hubProduct)
                .where(
                        hubProduct.company.id.eq(companyId),
                        hub.deletedAt.isNull(),
                        hubProduct.deletedAt.isNull()
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public boolean isDuplicatedItem(UUID companyId) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(hubProduct)
                .where(
                        hubProduct.company.id.eq(companyId),
                        hubProduct.deletedAt.isNull()
                )
                .fetchFirst();
        return fetchOne != null;
    }
}
