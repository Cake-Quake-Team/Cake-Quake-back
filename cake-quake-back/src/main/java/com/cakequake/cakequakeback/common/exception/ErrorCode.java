package com.cakequake.cakequakeback.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    //HTTP 400 BAD Request (600~650)
    INVALID_USER_ID(400, 601, "아이디가 조건에 불충족할 경우"),
    INVALID_PASSWORD(400, 602, "비밀번호가 조건에 불충족할 경우"),
    INVALID_NAME_SHORT(400, 603, "이름 또는 닉네임(20자)이 조건에 불충족할 경우"),
    INVALID_PHONE(400, 604, "전화번호 형식이 올바르지 않습니다"),
    INVALID_PRIVACY_SETTING(400, 605, "정보공개 여부 값이 유효하지 않습니다"),
    INVALID_SIGNUP_TYPE(400, 606, "가입 유형이 조건에 불충족할 경우"),
    INVALID_BUSINESS_NUMBER(400, 607, "사업자 등록 번호 형식이 올바르지 않습니다"),
    INVALID_COMPANY_NAME(400, 608, "상호명이 조건에 불충족할 경우"),
    INVALID_OWNER_NAME(400, 609, "대표자명이 조건에 불충족할 경우"),
    INVALID_FILE_TYPE(400, 610, "파일 유형이 잘못되었습니다"),
    MISSING_TEMP_SELLER_ID(400, 611, "tempSellerId가 누락되었습니다"),
    INVALID_SHOP_IMAGE(400, 612, "매장 이미지 형식이 유효하지 않습니다"),
    INVALID_SHOP_ADDRESS(400, 613, "매장 주소가 조건에 불충족할 경우"),
    INVALID_SHOP_NAME(400, 614, "매장명이 조건에 불충족할 경우"),
    INVALID_SHOP_PHONE(400, 615, "매장 전화번호 형식이 올바르지 않습니다"),
    INVALID_BUSINESS_HOURS(400, 616, "영업시간이 조건에 불충족할 경우"),
    MISSING_CAKE_ITEM_ID(400, 617, "상품ID가 누락되었습니다"),
    INVALID_CERTIFICATE(400, 618, "위생 인증서 형식이 올바르지 않습니다"),
    INVALID_NOTIFICATION_SETTING(400, 619, "알람 설정 값이 유효하지 않습니다"),
    MISSING_REFRESH_TOKEN(400, 620, "refresh 토큰이 누락되었습니다"),
    INVALID_DUPLICATION_CHECK(400, 621, "중복확인 요청 양식이 잘못되었습니다"),
    INVALID_OTP_FORMAT(400, 622, "OTP 형식이 잘못되었습니다"),
    MISSING_RESET_TOKEN(400, 623, "reset 토큰이 누락되었습니다"),
    INVALID_SHOP_ID(400, 624, "shopId가 누락되었거나 숫자가 아닙니다"),
    INVALID_SHEET_SHAPE(400, 625, "시트 모양이 누락되었거나 조건에 불충족할 경우"),
    INVALID_SHEET_SIZE(400, 626, "시트 크기가 조건에 불충족할 경우"),
    INVALID_CAKE_LAYER(400, 627, "케이크 단 수가 조건에 불충족할 경우"),
    INVALID_PRICE(400, 628, "가격이 조건에 불충족할 경우"),
    INVALID_LONG_NAME(400, 629, "이름이 너무 긴 경우"),
    INVALID_TYPE(400, 630, "타입이 허용된 값(INNER, OUTER)이 아닙니다"),
    INVALID_PAGE_SIZE(400, 631, "page, size는 1 이상이어야 합니다"),
    INVALID_CATEGORY(400, 632, "category 값이 누락되었거나 잘못되었습니다"),
    MISSING_SHORT_DESCRIPTION(400, 633, "짧은 설명이 누락되었습니다(1~300자)"),
    MISSING_LONG_DESCRIPTION(400, 634, "긴 설명이 누락되었습니다(1~1000자)"),
    INSUFFICIENT_POINTS(400, 635, "포인트가 부족합니다"),
    INVALID_POINT_VALUE(400, 636, "포인트 값이 조건에 불충족할 경우"),
    INVALID_ORDER_ID(400, 637, "orderId가 조건에 불충족할 경우"),
    INVALID_POINT_TYPE(400, 638, "적립 타입이 유효하지 않습니다"),
    INVALID_REVIEW_ID_FORMAT(400, 639, "reviewId 형식 오류"),
    MISSING_REVIEW_CONTENT(400, 640, "리뷰 평점과 내용이 입력되지 않았습니다"),
    EMPTY_CART_OR_BOTH_FLOWS(400, 641, "장바구니가 비어있거나 두 흐름이 모두 선택되었습니다"),
    INVALID_DATETIME_FORMAT(400, 642, "날짜 또는 시간 형식이 올바르지 않습니다"),
    INVALID_TIME_RANGE(400, 643, "startTime이 endTime 이전이어야 합니다"),
    QUANTITY_LIMIT_EXCEEDED(400, 644, "수량 제한을 초과했습니다"),
    OUT_OF_STOCK_OPTION(400, 645, "품절된 옵션을 선택했습니다"),
    MISSING_CART_ID(400, 646, "CartID가 누락되었습니다"),
    INVALID_CART_ITEMS(400, 647, "cartItemIds와 directItems에 유효하지 않은 항목이 포함되었습니다"),
    MISSING_SCHEDULE_ID(400, 648, "scheduleId가 누락되었습니다"),
    MISSING_RESERVATION_ID(400, 649, "reservationId가 누락되었습니다"),
    INVALID_PAYMENT_METHOD(400, 650, "결제 수단이 유효하지 않거나 지원하지 않습니다"),

    //HTTP 401 Unauthorized (code:801~809)
    INVALID_CREDENTIALS(401, 801, "아이디 또는 패스워드가 일치하지 않습니다"),
    MISSING_JWT(401, 802, "JWT 토큰이 존재하지 않거나 형식이 올바르지 않습니다"),
    INVALID_JWT(401, 803, "유효하지 않은 JWT 토큰입니다"),
    EXPIRED_JWT(401, 804, "만료된 JWT 토큰입니다"),
    INVALID_REFRESH_TOKEN(401, 805, "refresh 토큰이 잘못되었거나 없습니다"),
    EXPIRED_REFRESH_TOKEN(401, 806, "유효하지 않은 refresh 토큰입니다"),
    INVALID_OTP(401, 807, "잘못된 인증번호입니다"),
    EXPIRED_OTP(401, 808, "만료된 인증번호입니다"),
    INVALID_PG_SIGNATURE(401, 809, "PG사 서명이 유효하지 않습니다"),


    //HTTP 403 Forbidden (code:901~911)
    NOT_SELLER(403, 901, "판매자가 호출했습니다"),
    NOT_BUYER(403, 902, "구매자가 호출했습니다"),
    ALREADY_LOGGED_OUT(403, 903, "이미 로그아웃된 토큰입니다"),
    NO_ADMIN_ROLE(403, 904, "관리자 권한이 필요합니다"),
    NOT_OWN_ORDER(403, 905, "주문 번호가 본인의 것이 아닙니다"),
    NOT_AUTHORIZED_OTHER(403, 906, "권한이 없습니다"),
    NO_SHOP_ACCESS(403, 907, "해당 매장에 대한 권한이 없습니다"),
    ORDER_MISMATCH(403, 908, "주문서가 사용자와 일치하지 않습니다"),
    NOT_OWN_SHOP_REVIEW(403, 909, "본인 매장의 리뷰가 아닙니다"),
    NOT_AUTHORIZED_OTHER_SELLER(403, 910, "다른 판매자가 호출했습니다"),
    NOT_OWN_COUPON(403, 911, "본인의 쿠폰이 아닙니다"),


    //HTTP 404 Not Found (code:1001 ~1003)
    NOT_FOUND_TEMP_SELLER_ID   (404, 1001, "tempSellerId가 존재하지 않습니다"),
    NOT_FOUND_OPTION_ID        (404, 1002, "옵션 ID가 존재하지 않습니다"),
    NOT_FOUND_SHOP_ID          (404, 1003, "해당 매장을 찾을 수 없습니다"),
    NOT_FOUND_REVIEW_ID        (404, 1004, "해당 리뷰를 찾을 수 없습니다"),
    NOT_FOUND_UID              (404, 1005, "UID가 존재하지 않습니다"),
    NOT_FOUND_PRODUCT_ID       (404, 1006, "해당 상품을 찾을 수 없습니다"),
    NOT_FOUND_POINT_HISTORY    (404, 1007, "포인트 내역이 없습니다"),
    NOT_FOUND_ORDER_ID         (404, 1008, "해당 주문 정보를 찾을 수 없습니다"),
    NOT_FOUND_SCHEDULE_ID      (404, 1009, "해당 scheduleId가 존재하지 않습니다"),
    NOT_FOUND_CART_ID          (404, 1010, "해당 CartID가 존재하지 않습니다"),
    NOT_FOUND_RESERVATION_ID   (404, 1011, "해당 reservationId가 존재하지 않습니다"),
    INVALID_COUPON             (404, 1012, "유효하지 않은 쿠폰입니다"),
    NOT_FOUND_PAYMENT_ID       (404, 1013, "해당 결제 ID가 존재하지 않습니다"),
    DELETION_REQUEST_NOT_FOUND(404,1014, "해당 삭제 내역을 찾을 수 없습니다"),

    //HTTP 409 Conflict (code: 701~715)
    ALREADY_EXIST_USER_ID      (409, 701, "이미 존재하는 아이디입니다"),
    ALREADY_EXIST_PHONE        (409, 702, "이미 가입된 전화번호입니다"),
    ALREADY_EXIST_BUSINESS_NUM (409, 703, "이미 등록된 사업자 번호입니다"),
    ALREADY_EXIST_SHOP         (409, 704, "이미 존재하는 매장입니다"),
    ACTIVE_ORDER_EXISTS        (409, 705, "진행 중인 주문이 있습니다"),
    ACTIVE_SHOP_WITHDRAWAL     (409, 706, "승인 중인 매장이 있어 탈퇴가 불가능합니다"),
    OPERATING_SHOP_CANNOT_WITHDRAW(409,707,"운영 중인 매장이 있어 탈퇴가 불가능합니다"),
    ALREADY_REVIEWED_ORDER     (409, 708, "해당 주문에는 이미 리뷰가 작성되었습니다"),
    ALREADY_EXIST_CHAT_ROOM    (409, 709, "이미 동일한 채팅방이 존재합니다"),
    ALREADY_DELETION_REQUEST   (409, 710, "이미 삭제 요청된 리뷰입니다"),
    ALREADY_CANCELLED_REQUEST  (409, 711, "이미 취소된 상태입니다"),
    ALREADY_BOOKED_TIME        (409, 712, "이미 예약된 시간입니다"),
    ALREADY_PAID_PRODUCT       (409, 713, "이미 결제된 상품입니다"),
    ALREADY_USED_COUPON        (409, 714, "이미 사용된 쿠폰입니다"),
    EXCEEDED_COUPON_LIMIT      (409, 715, "쿠폰 2개 이상 사용할 수 없습니다"),

    //HTTP 429 Too Many Request (code:1101)
    EXCEEDED_SMS_LIMIT         (429,1101,"SMS 요청 한도를 초과했습니다"),

    //HTTP 500 InternalServer Error & 502 Bad Gateway
    INTERNAL_SERVER_ERROR      (500,1500,"서버 내부 오류입니다"),
    BAD_GATEWAY                (502,1502,"외부 서비스(PG)와 연결이 끊겼습니다");


    private final int httpStatus;
    private final int code;
    private final String message;

    ErrorCode(int httpStatus, int code, String message){
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
