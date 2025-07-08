package com.cakequake.cakequakeback.qna.controller;

import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.qna.dto.QnAAdminRequestDTO;
import com.cakequake.cakequakeback.qna.dto.QnAResponseDTO;
import com.cakequake.cakequakeback.qna.entities.QnAStatus;
import com.cakequake.cakequakeback.qna.entities.QnAType;
import com.cakequake.cakequakeback.qna.service.QnAService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/qna")
@RequiredArgsConstructor
public class QnAAdminController {
    private final QnAService qnaService;

    //전체 문의
    @GetMapping
    public Page<QnAResponseDTO> listAll(
            @ModelAttribute PageRequestDTO pageRequestDTO
            ){
        Pageable pageable = pageRequestDTO.getPageable("regDate");
        return qnaService.listAll(pageable);
    };

    @GetMapping("/type/{type}")
    public Page<QnAResponseDTO> listByType(
            @PathVariable QnAType type,
            @ModelAttribute PageRequestDTO pageRequestDTO
            ){
        Pageable pageable = pageRequestDTO.getPageable("regDate");
        return qnaService.listByType(type, pageable);
    }

    //상태별 문의
    @GetMapping("/status/{status}")
    public Page<QnAResponseDTO> listByType(
            @PathVariable QnAStatus status,
            @ModelAttribute PageRequestDTO pageRequestDTO
    ){
        Pageable pageable = pageRequestDTO.getPageable("regDate");
        return qnaService.listByStatus(status, pageable);
    }

    //작성자 역활별 문의
    @GetMapping("role/{role}")
    public Page<QnAResponseDTO> listByRole(
            @PathVariable MemberRole role,
            @ModelAttribute PageRequestDTO pageRequestDTO
            ){
        Pageable pageable = pageRequestDTO.getPageable("regDate");
        return qnaService.listBuAuthorRole(role, pageable);
    }

    //관리자 답변 등록
    @PostMapping("/respond")
    public void respondToQnA(
            @RequestBody QnAAdminRequestDTO qnaAdminRequestDTO
    ){
        qnaService.respondToQnA(qnaAdminRequestDTO);
    }

    //단건 조회
    @GetMapping("/{qnaId}")
    public QnAResponseDTO getDetail(
            @PathVariable Long qnaId
    ){
        return qnaService.getDetailForAdmin(qnaId);
    }

}
