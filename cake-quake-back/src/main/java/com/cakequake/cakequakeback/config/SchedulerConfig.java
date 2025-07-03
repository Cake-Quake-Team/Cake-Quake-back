package com.cakequake.cakequakeback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling // 스케줄링 기능 활성화
public class SchedulerConfig {

    @Bean // TaskScheduler 빈 등록
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10); // 적절한 스레드 풀 크기
        scheduler.setThreadNamePrefix("pickup-reminder-scheduler-");
        scheduler.initialize();  // 스케줄러 초기화
        return scheduler;  // 생성된 TaskScheduler 객체를 Spring 빈으로 반환
    }
}