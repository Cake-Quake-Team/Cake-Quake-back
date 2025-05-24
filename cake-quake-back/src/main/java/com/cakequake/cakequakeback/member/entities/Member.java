package com.cakequake.cakequakeback.member.entities;


import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table( name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {
    @Id
    @GeneratedValue ( strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    @SequenceGenerator(
            name = "user_seq_gen",       // JPA 내부에서 쓸 식별자
            sequenceName = "user_seq",   // DB에 생성된 시퀀스 이름
            initialValue = 1,            // 시퀀스 START WITH 값
            allocationSize = 50          // 한 번에 미리 가져올 시퀀스 수
    )
    private Long uid;

    @Column(name = "uname" , nullable = false)
    private String uname;

    @Column(name = "userId" , nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(unique = true)
    private String socialId;    // 소셜에서 제공한 고유 ID

    @Enumerated(EnumType.STRING)
    private SocialType socialType;  // GOOGLE, KAKAO 등

    @Column(nullable = false)
    private Boolean alarm = true;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MemberDetail memberDetail;

    public void setRole(MemberRole role) {
        this.role = role;
    }

}
