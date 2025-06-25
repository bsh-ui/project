// src/main/java/com/boot/service/NoticeService.java
package com.boot.service;

import com.boot.domain.Notice;
import com.boot.domain.NoticeType; // NoticeType 임포트 추가 (필수)
import com.boot.domain.User;
import com.boot.dto.NoticeCreateRequestDto; // DTO 임포트 추가 (필수)
import com.boot.dto.NoticeDto; // DTO 임포트 추가 (필수)
import com.boot.repository.NoticeRepository;
import com.boot.repository.UserRepository; // UserRepository 임포트 (필수)
import jakarta.persistence.EntityNotFoundException; // 임포트 추가 (필수)
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    // 1. 게시글 목록 조회 (페이징 및 타입별)
    @Transactional(readOnly = true)
    public Page<NoticeDto> getNoticeList(NoticeType type, Pageable pageable) { // ⭐ String -> NoticeType 변경
        Page<Notice> noticesPage;
        if (type != null) { // 특정 타입으로 필터링
            noticesPage = noticeRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
        } else { // 모든 타입 조회
            noticesPage = noticeRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return noticesPage.map(NoticeDto::fromEntity); // ⭐ Page<Notice>를 Page<NoticeDto>로 변환하여 반환
    }

    // 2. 특정 게시글 상세 조회 및 조회수 증가
    @Transactional
    public NoticeDto getNoticeById(Long id) { // ⭐ 반환 타입을 Notice -> NoticeDto로 변경
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("공지사항/이벤트를 찾을 수 없습니다: " + id)); // ⭐ IllegalArgumentException -> EntityNotFoundException

        notice.incrementViewCount(); // 조회수 증가 (자동 저장됨)
        // note: @Transactional이므로, 엔티티의 변경은 트랜잭션 종료 시 자동으로 DB에 반영됩니다.
        // 따라서 noticeRepository.save(notice)를 명시적으로 호출할 필요가 없습니다.

        return NoticeDto.fromEntity(notice); // ⭐ DTO로 변환하여 반환
    }

    // 3. 새 게시글 생성 (기존 코드에서 매개변수를 DTO로 변경)
    @Transactional
    public NoticeDto createNotice(NoticeCreateRequestDto requestDto, String username) { // ⭐ 매개변수 DTO 사용 및 반환 타입 DTO로 변경
        // JWT 인증 필터에서 가져온 username으로 User 엔티티 조회 (작성자 연결)
        User author = userRepository.findByUsername(username) // ⭐ authorId 대신 username으로 User 조회
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));

        Notice notice = Notice.builder()
                .title(requestDto.getTitle()) // ⭐ requestDto에서 값 가져오기
                .content(requestDto.getContent()) // ⭐ requestDto에서 값 가져오기
                .author(author)
                .type(requestDto.getType() != null ? requestDto.getType() : NoticeType.NOTICE) // ⭐ Enum 타입 사용 및 기본값 NOTICE
                .build();

        Notice savedNotice = noticeRepository.save(notice);
        return NoticeDto.fromEntity(savedNotice); // ⭐ DTO로 변환하여 반환
    }

    // 4. 게시글 수정 (기존 코드에서 매개변수를 DTO로 변경)
    @Transactional
    public NoticeDto updateNotice(Long id, NoticeCreateRequestDto requestDto) { // ⭐ 매개변수 DTO 사용, 반환 타입 DTO로 변경
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notice not found with ID: " + id)); // ⭐ IllegalArgumentException -> EntityNotFoundException

        // DTO에서 받은 값으로 엔티티 업데이트
        notice.update(requestDto.getTitle(), requestDto.getContent(),
                      requestDto.getType() != null ? requestDto.getType() : NoticeType.NOTICE); // ⭐ Enum 타입 사용 및 기본값

        // note: @Transactional이므로, 엔티티의 변경은 트랜잭션 종료 시 자동으로 DB에 반영됩니다.
        // 따라서 noticeRepository.save(notice)를 명시적으로 호출할 필요가 없습니다.
        return NoticeDto.fromEntity(notice); // ⭐ DTO로 변환하여 반환
    }

    // 5. 게시글 삭제
    @Transactional
    public void deleteNotice(Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new EntityNotFoundException("Notice not found with ID: " + id); // ⭐ IllegalArgumentException -> EntityNotFoundException
        }
        noticeRepository.deleteById(id);
    }
}