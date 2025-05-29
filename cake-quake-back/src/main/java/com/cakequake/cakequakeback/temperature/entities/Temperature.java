package com.cakequake.cakequakeback.temperature.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Builder
@Entity
@Table( name = "temperature")
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class Temperature extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long uid;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId           // Member의 PK를 공유
    @JoinColumn(name = "uid", nullable = false)
    private Member member; //Member의 UID가 곧 Temperature ID

    @Column(nullable = false)
    private double temperature = 36.5; //현재 매너 온도

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Grade grade; //온도 따라 산정된 등급

    @Column(nullable = false)
    private float changeAmount; //온도 변화량

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChangeReason reason; //온도 변화의 원인

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RelatedObjectType relatedObjectType; //온도 변화에 영향 준 객체

    @Column(nullable = false)
    private String relatedObjectId; //객체와 연결된 객체 id(출처 추적)

    @OneToMany(mappedBy = "temperature", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TemperatureHistory> historyList = new ArrayList<>();

    //온도 업데이트
    public void updateTemperature(float changeAmount,ChangeReason reason,RelatedObjectType type, String objectId) {

        double newTemperature = this.temperature + changeAmount;
        if (changeAmount == 0 || this.temperature == newTemperature) return;
        this.temperature = newTemperature;
        this.changeAmount = changeAmount;

        this.reason = reason;
        this.relatedObjectType = type;
        this.relatedObjectId = objectId;



        //등급도 자동 계산
        this.grade =Grade.fromTemperature(this.temperature);

        //이력 추가
        this.historyList.add(new TemperatureHistory(this, changeAmount, (float)this.temperature, reason, type, objectId));

    }
}
