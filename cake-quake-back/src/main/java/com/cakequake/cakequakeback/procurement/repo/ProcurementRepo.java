package com.cakequake.cakequakeback.procurement.repo;

import com.cakequake.cakequakeback.procurement.dto.procurement.ProcurementResponseDTO;
import com.cakequake.cakequakeback.procurement.entities.Procurement;
import com.cakequake.cakequakeback.procurement.entities.ProcurementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//매장 -> 재료 요청 무한 스크롤 조회용
public interface ProcurementRepo extends JpaRepository<Procurement, Long> {

    /** 매장별 요청 내역 조회 (무한 스크롤용) */
    @Query("""
        SELECT new com.cakequake.cakequakeback.procurement.dto.procurement.ProcurementResponseDTO
            (
            p.procurementId,
            p.shop.shopId,
            p.shop.shopName,
            p.status,
            p.note,
            p.estimatedArrivalDate,
            p.regDate,
                p.cancelReason,
            null
        )
        FROM Procurement p
        WHERE p.shop.shopId = :shopId
        ORDER BY p.procurementId DESC
    """)
    Page<ProcurementResponseDTO> listByShop(
            @Param("shopId") Long shopId,
            Pageable pageable
    );

    /** 상태별 요청 내역 조회 */
    @Query("""
        SELECT new com.cakequake.cakequakeback.procurement.dto.procurement.ProcurementResponseDTO(
            p.procurementId,
            p.shop.shopId,
            p.shop.shopName,
            p.status,
            p.note,
            p.estimatedArrivalDate,
            p.regDate,
                p.cancelReason,
            null
        )
        FROM Procurement p
        WHERE p.status = :status
        ORDER BY p.procurementId DESC
    """)
    Page<ProcurementResponseDTO> listByStatus(
            @Param("status") ProcurementStatus status,
            Pageable pageable
    );

    /** 매장+상태 복합 조회 */
    @Query("""
        SELECT new com.cakequake.cakequakeback.procurement.dto.procurement.ProcurementResponseDTO(
            p.procurementId,
            p.shop.shopId,
            p.shop.shopName,
            p.status,
            p.note,
            p.estimatedArrivalDate,
            p.regDate,
                p.cancelReason,
            null
        )
        FROM Procurement p
        WHERE p.shop.shopId = :shopId
          AND p.status      = :status
        ORDER BY p.procurementId DESC
    """)
    Page<ProcurementResponseDTO> listByShopAndStatus(
            @Param("shopId") Long shopId,
            @Param("status") ProcurementStatus status,
            Pageable pageable
    );


    //헤더 +아이템을 조인해서 한 번에 조회하는 Projection쿼리
    @Query("""
        SELECT p.procurementId       AS procurementId,
             p.shop.shopId        AS shopId,
                 p.shop.shopName,
               p.status             AS status,
              p.note               AS note,
               p.estimatedArrivalDate      AS scheduledDate,
               p.regDate          AS regDate,
                   p.cancelReason,
              pi.procurementItemId AS procurementItemId,
               pi.ingredient.ingredientId      AS ingredientId,
               pi.quantity          AS quantity
          FROM Procurement p
          JOIN ProcurementItem pi
        WHERE p.shop.shopId = :shopId
     ORDER BY p.procurementId DESC
    """)
    Page<com.cakequake.cakequakeback.procurement.projection.ProcurementWithItem>
    findWithItemsByShop(@Param("shopId") Long shopId, Pageable pageable);
}
