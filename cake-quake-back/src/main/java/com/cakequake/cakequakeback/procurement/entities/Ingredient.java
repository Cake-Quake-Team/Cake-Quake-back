package com.cakequake.cakequakeback.procurement.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ingredient")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ingredientId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false,length = 20)
    private String unit;

    @Column(length = 255)
    private String description;
}
