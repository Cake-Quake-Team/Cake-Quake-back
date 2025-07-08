package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerPageRequestDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerRequestListDTO;
import com.cakequake.cakequakeback.member.entities.PendingSellerRequest;
import com.cakequake.cakequakeback.member.entities.QPendingSellerRequest;
import com.cakequake.cakequakeback.member.entities.SellerRequestStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Slf4j
public class PendingSellerRequestCustomRepositoryImpl implements PendingSellerRequestCustomRepository{

    private final JPQLQueryFactory queryFactory;

    public PendingSellerRequestCustomRepositoryImpl(JPQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public InfiniteScrollResponseDTO<PendingSellerRequestListDTO> pendingSellerRequestList(PendingSellerPageRequestDTO pageRequestDTO) {
        QPendingSellerRequest qPendingSellerRequest = QPendingSellerRequest.pendingSellerRequest;

        JPQLQuery<PendingSellerRequest> query = queryFactory.selectFrom(qPendingSellerRequest);

        // 검색 조건
        String type = pageRequestDTO.getType();
        String keyword = pageRequestDTO.getKeyword();
        log.debug("type: {}", type);
        log.debug("keyword: {}", keyword);

        BooleanBuilder builder = new BooleanBuilder();
        if (keyword != null && !keyword.isEmpty() && type != null) {

            switch (type) {
                case "USERID": // userId 검색
                    builder.or(qPendingSellerRequest.userId.containsIgnoreCase(keyword));
                    break;
                case "UNAME": // uname 검색
                    builder.or(qPendingSellerRequest.uname.containsIgnoreCase(keyword));
                    break;
                case "SHOPNAME": // shopName 검색
                    builder.or(qPendingSellerRequest.shopName.containsIgnoreCase(keyword));
                    break;
            }

        } // end if

        // 상태 필터 추가
        String statusFilter = pageRequestDTO.getStatus(); // 프론트에서 보내는 status 값
//        log.debug("pageRequestDTO.getStatus(): {}", pageRequestDTO.getStatus());
        if (statusFilter != null && !statusFilter.isEmpty()) {
            builder.and(qPendingSellerRequest.status.eq(SellerRequestStatus.valueOf(statusFilter)));
        }

//        log.debug("builder: {}", builder);
        // 조건이 있는 경우에만 where 호출
        if (builder.hasValue()) {
            query.where(builder);
        }

        Pageable pageable = pageRequestDTO.getPageable("tempSellerId");
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());
        query.orderBy(qPendingSellerRequest.tempSellerId.desc());

        JPQLQuery<PendingSellerRequestListDTO> dtoQuery = query.select(Projections.bean(PendingSellerRequestListDTO.class,
                qPendingSellerRequest.tempSellerId,
                qPendingSellerRequest.userId,
                qPendingSellerRequest.uname,
                qPendingSellerRequest.phoneNumber,
                qPendingSellerRequest.businessNumber,
                qPendingSellerRequest.bossName,
                qPendingSellerRequest.openingDate,
                qPendingSellerRequest.shopName,
                qPendingSellerRequest.socialType,
                qPendingSellerRequest.publicInfo,
                qPendingSellerRequest.businessCertificateUrl,
                qPendingSellerRequest.status,
                qPendingSellerRequest.address,
                qPendingSellerRequest.openTime,
                qPendingSellerRequest.closeTime,
                qPendingSellerRequest.mainProductDescription,
                qPendingSellerRequest.shopPhoneNumber,
                qPendingSellerRequest.shopImageUrl,
                qPendingSellerRequest.sanitationCertificateUrl,
                qPendingSellerRequest.regDate,
                qPendingSellerRequest.modDate
        ));
//        log.debug("Generated DTO Query: {}", dtoQuery);

        List<PendingSellerRequestListDTO> dtoList = dtoQuery.fetch();
//        log.debug("Fetched DTO List: {}", dtoList); // DTO 리스트 확인

        // 전체 개수 쿼리 (페이지 계산용)
        long total = dtoQuery.fetchCount();
//        log.debug("total: {}", total);

        return InfiniteScrollResponseDTO.of(dtoList, (int) total, pageRequestDTO);
    }
}
