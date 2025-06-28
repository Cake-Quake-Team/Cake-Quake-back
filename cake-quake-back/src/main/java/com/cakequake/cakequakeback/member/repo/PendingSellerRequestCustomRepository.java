package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerPageRequestDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerRequestListDTO;

public interface PendingSellerRequestCustomRepository {
    InfiniteScrollResponseDTO<PendingSellerRequestListDTO> pendingSellerRequestList(PendingSellerPageRequestDTO pageRequestDTO);
}
