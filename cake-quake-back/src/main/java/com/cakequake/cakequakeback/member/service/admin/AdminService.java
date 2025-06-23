package com.cakequake.cakequakeback.member.service.admin;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerRequestListDTO;

public interface AdminService {

    InfiniteScrollResponseDTO<PendingSellerRequestListDTO> pendingSellerRequestList(PageRequestDTO pageRequestDTO);

    ApiResponseDTO approvePendingSeller(Long tempSellerId);

}
