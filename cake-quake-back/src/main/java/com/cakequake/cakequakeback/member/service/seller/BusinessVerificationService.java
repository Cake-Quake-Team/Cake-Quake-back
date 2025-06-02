package com.cakequake.cakequakeback.member.service.seller;

import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.verification.BusinessVerificationRequestDTO;

public interface BusinessVerificationService {

    ApiResponseDTO verify(BusinessVerificationRequestDTO dto);
}
