package com.cakequake.cakequakeback.badge.condition;

import com.cakequake.cakequakeback.member.entities.Member;

public interface BadgeCondition {

    Long getBadgeId();

    boolean isEligible(Member member);
}
