package com.cakequake.cakequakeback.temperature.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Table( name = "temperature_history")
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class TemperatureHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long HistoryID;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="uid",nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "temperature_id", nullable = false)
    private Temperature temperature;

    @Column(nullable = false)
    private float changeAmount; //온도 변화량

    @Column(nullable = false)
    private double afterTemperature; //변화 후 온도

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChangeReason reason; //온도 변화의 원인

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RelatedObjectType relatedObjectType; //온도 변화에 영향 준 객체

    @Column(nullable = false)
    private String relatedObjectId; //객체와 연결된 객체 id(출처 추적)

    public TemperatureHistory(Temperature temperature, float changeAmount, float afterTemperature,
                              ChangeReason reason, RelatedObjectType relatedObjectType, String relatedObjectId) {
        this.temperature = temperature;
        this.changeAmount = changeAmount;
        this.afterTemperature = afterTemperature;
        this.reason = reason;
        this.relatedObjectType = relatedObjectType;
        this.relatedObjectId = relatedObjectId;
    }
}
