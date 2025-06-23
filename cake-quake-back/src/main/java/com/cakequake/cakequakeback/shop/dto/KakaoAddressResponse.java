package com.cakequake.cakequakeback.shop.dto;

import java.util.List;


public class KakaoAddressResponse {
    public List<Document> documents;

    public static class Document {
        public String address_name;
        public String x; // 경도
        public String y; // 위도
    }
}

