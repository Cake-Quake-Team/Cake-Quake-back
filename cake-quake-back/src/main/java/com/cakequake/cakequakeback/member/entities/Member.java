package com.cakequake.cakequakeback.member.entities;


import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.dto.AlarmSettingsDTO;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Table( name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Column(nullable = false, length = 20)
    private String uname;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(unique = true)
    private String socialId;    // 소셜에서 제공한 고유 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialType socialType;  // GOOGLE, KAKAO, BASIC 등

    @Builder.Default
    @Column(nullable = false)
    private Boolean alarm = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean publicInfo = true;

    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber;

    // 탈퇴 여부
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MemberDetail memberDetail;

    // 수정용
    public void changeUname(String uname) {
        this.uname = uname;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void changePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void changeSocialId(String socialId) {
        this.socialId = socialId;
    }

    public void changeSocialType(SocialType socialType) {
        this.socialType = socialType;
    }

    public Boolean changeAlarm(AlarmSettingsDTO alarmSettings) {
        // 알람 설정을 변경
        if (alarmSettings != null) {
            this.alarm = alarmSettings.isAllAlarm(); // 전체 알람 설정 변경
            return true; // 성공적으로 변경됨
        }
        return false; // 변경 실패
    }

    // 탈퇴 시
    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
    }
    // 관리자 기능 용
    public void changeStatus(MemberStatus status) {
        this.status = status;
    }

    public void changeMemberDetail(MemberDetail memberDetail) {
        this.memberDetail = memberDetail;
    }

}
