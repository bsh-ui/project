package com.boot.repository;

import com.boot.domain.UserActivityLog; // UserActivityLog 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository; // JpaRepository 임포트
import org.springframework.stereotype.Repository; // Repository 어노테이션 임포트

@Repository // 이 인터페이스가 Spring Bean으로 등록되어 데이터베이스 상호작용을 처리하도록 지정
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    // JpaRepository를 상속받음으로써 UserActivityLog 엔티티에 대한 기본적인 CRUD 메서드(예: save(), findById(), findAll(), delete() 등)가 자동으로 제공됩니다.

    // 필요하다면 여기에 추가적인 쿼리 메서드를 정의할 수 있습니다.
    // 예를 들어, 특정 사용자의 활동 로그를 시간 순서대로 정렬하여 가져오는 메서드를 추가할 수 있습니다:
    // List<UserActivityLog> findByUserIdOrderByTimestampDesc(Long userId);

    // 혹은 특정 활동 유형의 로그를 조회하는 메서드:
    // List<UserActivityLog> findByActivityType(String activityType);
}