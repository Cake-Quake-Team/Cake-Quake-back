package com.cakequake.cakequakeback.member.service.seller;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep1RequestDTO;
import com.cakequake.cakequakeback.member.entities.SocialType;
import com.cakequake.cakequakeback.member.validator.MemberValidator;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class SellerServiceImpl implements SellerService{

    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberValidator memberValidator;

    public SellerServiceImpl(ShopRepository shopRepository, PasswordEncoder passwordEncoder, MemberValidator memberValidator) {
        this.shopRepository = shopRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberValidator = memberValidator;
    }

    @Override
    public ApiResponseDTO registerStepOne(SellerSignupStep1RequestDTO requestDTO) {

        SocialType joinType = SocialType.from(requestDTO.getJoinType());
        MultipartFile file = requestDTO.getBusinessCertificate();

        /*
            유효성 형식 검사 - userId, 비밀번호, uname 길이, 전화번호 형식, 가입 방식, 사업자 등록 번호, 대표자 성명, 개업 일자, 매장명
            중복 검사 - userId, 전화번호
        */
        memberValidator.validateSellerSignup(requestDTO);
        log.debug("---registerStepOne---memberValidator 통과---");

        // basic 가입일 때만 비밀번호 인코딩
        if(joinType == SocialType.BASIC) {
            String encodedPassword = passwordEncoder.encode(requestDTO.getPassword());
        }

        // 휴대폰 인증, 사업자 등록 진위여부 검증은 프론트에서 따로 호출

        /*
            파일 처리 - 사업자 등록증 파일
         */
        String uploadDir = "C:\\nginx-1.26.3\\html\\selleruploads";

        // 저장 경로
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs(); // 디렉토리 없으면 생성
        }

        // 파일이 비어있으면
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);   // 610
        }

        String contentType = file.getContentType();

        // 아미지 타입 확인
        if (contentType == null || !contentType.startsWith("image")) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        String originalName = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String savedName = uuid + "_" + originalName;

        File saveFile = new File(uploadDir, savedName);

        try {
            file.transferTo(saveFile);

        } catch (IOException e) {
//            throw new RuntimeException("리뷰 이미지 저장 중 오류 발생" + savedName, e);
        }


        // dto 저장

        return null;
    }
}
