package com.cakequake.cakequakeback.review.entities;

public enum DeletionRequestStatus {
        PENDING,  //매장 리뷰를 삭제해주세요
        APPROVED, //관리자가 요청을 검토한 뒤 삭제 허가를 내린 상태
        REJECTED  //관리자가 검토한 뒤 삭제 불가를 결정한 상태
}
