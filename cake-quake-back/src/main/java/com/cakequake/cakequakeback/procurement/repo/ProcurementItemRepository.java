package com.cakequake.cakequakeback.procurement.repo;

import com.cakequake.cakequakeback.procurement.entities.ProcurementItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcurementItemRepository extends JpaRepository<ProcurementItem, Long> {


    List<ProcurementItem> findByProcurement_ProcurementId(Long procurementId);

}
