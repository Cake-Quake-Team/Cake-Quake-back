package com.cakequake.cakequakeback.security.jwt;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class JWTClaimProvider {
    private final ShopRepository shopRepository;

    public JWTClaimProvider(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public Map<String, Object> createClaims(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", member.getUid());
        claims.put("userId", member.getUserId());
        claims.put("uname", member.getUname());
        claims.put("role", member.getRole().name());

        // 판매자일 경우 shopId 추가
        if (member.getRole() == MemberRole.SELLER) {
            shopRepository.findPreviewByUid(member.getUid()).ifPresent(shop -> {
                claims.put("shopId", shop.getShopId());
            });
        }

        return claims;
    }
}
