package com.cakequake.cakequakeback.review.entities;

public enum ReviewStatus {
    ACTIVE,  //등록과 동시에 리뷰가 화면에보임
    DELTE_REQUEST, //판매자가 삭제 요청
    DELETED //리뷰가 삭제 처리됨
}
