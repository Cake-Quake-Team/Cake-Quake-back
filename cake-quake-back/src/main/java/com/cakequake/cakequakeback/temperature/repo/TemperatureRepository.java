package com.cakequake.cakequakeback.temperature.repo;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.temperature.entities.Temperature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TemperatureRepository extends JpaRepository<Temperature, Long> {

    //uid 이용한 온도 조회
    @Query("SELECT t FROM Temperature t WHERE t.member = :member")
    Optional<Temperature> findByMember(@Param("member") Member member);


}
