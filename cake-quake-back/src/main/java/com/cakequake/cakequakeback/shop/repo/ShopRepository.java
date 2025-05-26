package com.cakequake.cakequakeback.shop.repo;

import com.cakequake.cakequakeback.shop.entities.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {
}
