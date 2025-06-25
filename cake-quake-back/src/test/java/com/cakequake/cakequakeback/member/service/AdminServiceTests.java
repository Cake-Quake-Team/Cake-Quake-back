//package com.cakequake.cakequakeback.member.service;
//
//import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
//import com.cakequake.cakequakeback.member.entities.Member;
//import com.cakequake.cakequakeback.member.entities.PendingSellerRequest;
//import com.cakequake.cakequakeback.member.repo.MemberRepository;
//import com.cakequake.cakequakeback.member.repo.PendingSellerRequestRepository;
//import com.cakequake.cakequakeback.member.service.admin.AdminService;
//import com.cakequake.cakequakeback.member.service.auth.MemberServiceImpl;
//import com.cakequake.cakequakeback.shop.entities.Shop;
//import com.cakequake.cakequakeback.shop.repo.ShopRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.test.context.TestPropertySource;
//
//@SpringBootTest
//@Import({MemberServiceImpl.class, BCryptPasswordEncoder.class})
//@Slf4j
//@TestPropertySource(properties = {
//        "logging.level.com.cakequake.cakequakeback.member=DEBUG",
//        "logging.level.root=INFO"
//})
//public class AdminServiceTests {
//
//    @Autowired
//    private  AdminService adminService;
//
//    @Autowired
//    private PendingSellerRequestRepository pendingSellerRequestRepository;
//
//    @Autowired
//    private MemberRepository memberRepository;
//
//    @Autowired
//    private ShopRepository shopRepository;
//
//
////    @Test
////    void testApprovePendingSeller() {
////
////        Long tempSellerId = 5L; // 실제 DB에 존재하는 tempSellerId
////
////        // when: 승인 메서드 호출
////        ApiResponseDTO response = adminService.approvePendingSeller(tempSellerId);
////
////        log.info("응답 메시지: {}", response.getMessage());
////
////        // log: 상태 변경 확인
////        PendingSellerRequest updatedRequest = pendingSellerRequestRepository.findById(tempSellerId).orElseThrow();
////        log.info("판매자 요청 상태: {}", updatedRequest.getStatus());
////
////        // log: 생성된 회원 확인
////        Member member = memberRepository.findByUserId(updatedRequest.getUserId()).orElse(null);
////        if (member != null) {
////            log.info("생성된 Member - UID: {}, 이름: {}, 역할: {}", member.getUid(), member.getUname(), member.getRole());
////        } else {
////            log.warn("Member가 생성되지 않았습니다.");
////        }
////
////        // log: 생성된 샵 확인
////        if (member != null) {
////            Shop shop = shopRepository.findActiveShopByUid(member.getUid()).orElse(null);
////            if (shop != null) {
////                log.info("생성된 Shop - 이름: {}, 주소: {}, 번호: {}", shop.getShopName(), shop.getAddress(), shop.getPhone());
////            } else {
////                log.warn("Shop이 생성되지 않았습니다.");
////            }
////        }
////    }
//}
