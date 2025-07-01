package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerPageRequestDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerRequestListDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class PendingSellerRequestCustomRepoTests {

    @Autowired
    private PendingSellerRequestRepository pendingSellerRequestRepository;

    @Test
    public void testProductSearchList() {

        PendingSellerPageRequestDTO requestDTO = new PendingSellerPageRequestDTO();
        requestDTO.setPage(1);
        requestDTO.setSize(10);
        requestDTO.setType("SHOPNAME");
        requestDTO.setKeyword("케이크");

        InfiniteScrollResponseDTO<PendingSellerRequestListDTO> result = pendingSellerRequestRepository.pendingSellerRequestList(requestDTO);

        log.info(String.valueOf(result));
        result.getContent().forEach(dto -> log.info(String.valueOf(dto)));
    }

}
