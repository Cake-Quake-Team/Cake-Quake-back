package com.cakequake.cakequakeback.member.service.admin;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerPageRequestDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerRequestListDTO;

public interface AdminService {

    InfiniteScrollResponseDTO<PendingSellerRequestListDTO> pendingSellerRequestList(PendingSellerPageRequestDTO pageRequestDTO);

    ApiResponseDTO approvePendingSeller(Long tempSellerId);         // 판매자 가입 승인

    ApiResponseDTO holdPendingSellerStatus(Long tempSellerId);      // 보류

    ApiResponseDTO rejectPendingSellerStatus(Long tempSellerId);    // 거절

}
