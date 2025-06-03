package com.cakequake.cakequakeback.member.service.seller;

import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep1RequestDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep2RequestDTO;

public interface SellerService {

    ApiResponseDTO registerStepOne(SellerSignupStep1RequestDTO dto);

    ApiResponseDTO registerStepTwo(SellerSignupStep2RequestDTO dto);
}
