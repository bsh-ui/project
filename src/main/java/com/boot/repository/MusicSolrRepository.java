//// src/main/java/com/boot/repository/MusicSolrRepository.java
//package com.boot.repository;
//
//import com.boot.domain.Music; // Music 엔티티가 Solr 문서 역할을 한다고 가정
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public interface MusicSolrRepository extends SolrCrudRepository<Music, Long> {
//
//    // 1. 단일 필드 검색 (제목 또는 아티스트)
//    Page<Music> findByTitleContainingOrArtistContaining(String titleKeyword, String artistKeyword, Pageable pageable);
//
//    // 2. 여러 필드에서 OR 조건으로 검색 (더 유연한 검색)
//    // q: 쿼리할 키워드, OR/AND 연산자 등 사용 가능
//    // fields: 검색할 필드 목록
//    // @Query("?0")는 첫 번째 파라미터를 쿼리 문자열로 사용
//    // 보통 Solr 스키마에 'text_general'과 같은 복합 필드를 만들고 그 필드로 검색하는 것이 더 효율적입니다.
//    // 여기서는 title, artist, album 필드를 대상으로 검색한다고 가정합니다.
//    @Query("title:*?0* OR artist:*?0* OR album:*?0*")
//    Page<Music> searchAllFields(String keyword, Pageable pageable);
//
//    // 추가: 특정 필드에 대한 정확한 일치 검색 (필요하다면)
//    // Page<Music> findByArtist(String artist, Pageable pageable);
//}