package com.boot.service;

import com.boot.domain.UserActivityLog; // UserActivityLog 엔티티 import
import com.boot.repository.UserActivityLogRepository; // UserActivityLogRepository import
import com.boot.util.ActivityType; // ⭐ ActivityType enum import 예정 (아직 없다면 다음 단계에서 생성)
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 활동 로그를 기록하는 서비스.
 * 비즈니스 로직을 캡슐화하고 리포지토리와 상호작용합니다.
 */
@Service
@RequiredArgsConstructor // Lombok을 사용하여 final 필드인 userActivityLogRepository를 자동 주입
public class UserActivityLogService {

    private static final Logger logger = LoggerFactory.getLogger(UserActivityLogService.class);

    private final UserActivityLogRepository userActivityLogRepository;

    /**
     * 사용자 활동 로그를 데이터베이스에 저장합니다.
     *
     * @param userId 활동을 수행한 사용자 ID
     * @param activityType 활동 유형 (ActivityType enum 사용 권장)
     * @param details 활동에 대한 상세 정보
     * @param ipAddress 활동이 발생한 IP 주소
     */
    @Transactional // 활동 로그 저장은 트랜잭션 내에서 이루어지도록 합니다.
    public void saveActivity(Long userId, ActivityType activityType, String details, String ipAddress) {
        if (userId == null) {
            logger.warn("⚠️ UserActivityLogService: User ID is null, skipping log saving for activityType={}", activityType);
            return; // 사용자 ID가 없으면 로그를 저장하지 않습니다.
        }

        UserActivityLog log = UserActivityLog.builder()
                .userId(userId)
                .activityType(activityType.name()) // Enum 이름을 String으로 변환하여 저장
                .ipAddress(ipAddress)
                .details(details)
                .build();

        try {
            userActivityLogRepository.save(log);
            logger.debug("✅ Activity Log saved successfully: UserID={}, Type={}, Details={}", userId, activityType.name(), details);
        } catch (Exception e) {
            logger.error("⚠️ Failed to save activity log for UserID={}: {}", userId, e.getMessage(), e);
            // 로그 저장 실패는 핵심 비즈니스 로직의 실패로 이어지지 않도록 예외를 다시 던지지 않는 것이 일반적입니다.
        }
    }

    // 필요하다면 특정 사용자의 활동 로그 조회, 특정 기간 활동 로그 조회 등
    // 추가적인 비즈니스 로직 메서드를 여기에 구현할 수 있습니다.
    /*
    @Transactional(readOnly = true)
    public List<UserActivityLog> getUserActivityLogs(Long userId, Pageable pageable) {
        return userActivityLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }
    */
}