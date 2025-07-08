package com.cakequake.cakequakeback.qna.controller;


import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.qna.dto.QnARequestDTO;
import com.cakequake.cakequakeback.qna.dto.QnAResponseDTO;
import com.cakequake.cakequakeback.qna.service.QnAService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/qna")
@RequiredArgsConstructor
public class QnAController {

    private final QnAService qnaService;

    //문의 작성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long createQna(
            @RequestBody @Valid  QnARequestDTO qnaRequestDTO,
            @AuthenticationPrincipal(expression = "member.uid") Long uid
            ){
        return qnaService.createQnA(qnaRequestDTO,uid);
        };

    //내 문의 전체 조회
    @GetMapping
    public InfiniteScrollResponseDTO<QnAResponseDTO> getMyQnAList(
            @AuthenticationPrincipal(expression = "member.uid")Long uid,
            @ModelAttribute PageRequestDTO pageRequestDTO
            ){
        return qnaService.getMyQnAList(pageRequestDTO,uid);
    }

    //내 문의 단건 조회
    @GetMapping("/{qnaId}")
    public QnAResponseDTO getMyQnA(
            @PathVariable Long qnaId,
            @AuthenticationPrincipal(expression = "member.uid" ) Long uid
    ){
        return qnaService.getMyQnA(qnaId,uid);
    }

    //내 문의 수정
    @PatchMapping("/{qnaId}")
    public QnAResponseDTO updateQna(
            @PathVariable Long qnaId,
            @RequestBody @Valid QnARequestDTO dto,
            @AuthenticationPrincipal(expression = "member.uid") Long uid
    ){
        return qnaService.updateQnA(qnaId,dto,uid);
    }


    //내 문의 삭제
    @DeleteMapping("/{qnaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQnA(
            @PathVariable Long qnaId,
            @AuthenticationPrincipal(expression = "member.uid") Long uid
    ){
        qnaService.deleteQnA(qnaId,uid);
    }
}
