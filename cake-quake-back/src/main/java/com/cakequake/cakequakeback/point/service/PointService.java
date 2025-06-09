package com.cakequake.cakequakeback.point.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.point.dto.PointHistoryResponseDTO;
import com.cakequake.cakequakeback.point.dto.PointResponseDTO;

public interface PointService {

    //특정 사용자의 현재 포인트 잔액을 조회
    Long getCurrentBalance(Long uid);

    //Point 엔티티가 없으면 새로 생성하고, 초기 잔액을 0으로 두고 증감
    //특정 사용자의 포인트를 증감합니다.
    Long changePoint(Long uid, Long amount, String description);

    InfiniteScrollResponseDTO<PointHistoryResponseDTO> getPointHistoryPage(PageRequestDTO pageRequestDTO, Long uid);

}
