package com.boot.service;

import com.boot.domain.MusicDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.client.solrj.util.ClientUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicSolrService {

    private final SolrClient solrClient;
    private static final String MUSIC_CORE_NAME = "music";

    /**
     * MusicDocument 객체를 Solr에 색인합니다.
     * @param musicDocument 색인할 MusicDocument 객체
     * @return 색인 성공 여부
     */
    public boolean indexMusicDocument(MusicDocument musicDocument) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", musicDocument.getId());
        doc.addField("title", musicDocument.getTitle());
        doc.addField("artist", musicDocument.getArtist());
        doc.addField("album", musicDocument.getAlbum());

        if (musicDocument.getLyrics() != null) {
            doc.addField("lyrics", musicDocument.getLyrics());
        }
        if (musicDocument.getGenre() != null && !musicDocument.getGenre().isEmpty()) {
            doc.addField("genre", musicDocument.getGenre());
        }
        if (musicDocument.getReleaseYear() != null) {
            doc.addField("release_year", musicDocument.getReleaseYear());
        }
        if (musicDocument.getPlayCount() != null) {
            doc.addField("play_count", musicDocument.getPlayCount());
        }
        if (musicDocument.getCategory() != null && !musicDocument.getCategory().isEmpty()) {
            doc.addField("category", musicDocument.getCategory());
        }
        if (musicDocument.getTags() != null && !musicDocument.getTags().isEmpty()) {
            doc.addField("tags", musicDocument.getTags());
        }
        if (musicDocument.getMusicUrl() != null) {
            doc.addField("music_url", musicDocument.getMusicUrl());
        }
        if (musicDocument.getDuration() != null) {
            doc.addField("duration", musicDocument.getDuration());
        }
        if (musicDocument.getCoverImagePath() != null) {
            doc.addField("cover_image_path", musicDocument.getCoverImagePath());
        }
        if (musicDocument.getUploadDate() != null) {
            doc.addField("uploadDate", musicDocument.getUploadDate());
        }
        // ⭐ 추가: Solr 스키마에 맞춰 createdAt, uploader_id, uploader_nickname 필드 추가 ⭐
        if (musicDocument.getCreatedAt() != null) {
            doc.addField("created_at", musicDocument.getCreatedAt());
        }
        if (musicDocument.getUploaderId() != null) {
            doc.addField("uploader_id", musicDocument.getUploaderId());
        }
        if (musicDocument.getUploaderNickname() != null) {
            doc.addField("uploader_nickname", musicDocument.getUploaderNickname());
        }

        try {
            UpdateResponse response = solrClient.add(MUSIC_CORE_NAME, doc);
            solrClient.commit(MUSIC_CORE_NAME);
            log.info("Solr 문서 색인 성공: ID={}, 상태={}", musicDocument.getId(), response.getStatus());
            return response.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            log.error("Solr 문서 색인 실패: ID={}, 에러: {}", musicDocument.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 여러 MusicDocument 객체를 한 번에 Solr에 색인합니다.
     * @param musicDocuments 색인할 MusicDocument 객체 리스트
     * @return 색인 성공 여부
     */
    public boolean indexAllMusicDocuments(List<MusicDocument> musicDocuments) {
        List<SolrInputDocument> docs = musicDocuments.stream()
            .map(musicDocument -> {
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", musicDocument.getId());
                doc.addField("title", musicDocument.getTitle());
                doc.addField("artist", musicDocument.getArtist());
                doc.addField("album", musicDocument.getAlbum());
                if (musicDocument.getLyrics() != null) doc.addField("lyrics", musicDocument.getLyrics());
                if (musicDocument.getGenre() != null && !musicDocument.getGenre().isEmpty()) doc.addField("genre", musicDocument.getGenre());
                if (musicDocument.getReleaseYear() != null) doc.addField("release_year", musicDocument.getReleaseYear());
                if (musicDocument.getPlayCount() != null) doc.addField("play_count", musicDocument.getPlayCount());
                if (musicDocument.getCategory() != null && !musicDocument.getCategory().isEmpty()) doc.addField("category", musicDocument.getCategory());
                if (musicDocument.getTags() != null && !musicDocument.getTags().isEmpty()) doc.addField("tags", musicDocument.getTags());
                if (musicDocument.getMusicUrl() != null) doc.addField("music_url", musicDocument.getMusicUrl());
                if (musicDocument.getDuration() != null) doc.addField("duration", musicDocument.getDuration());
                if (musicDocument.getCoverImagePath() != null) doc.addField("cover_image_path", musicDocument.getCoverImagePath());
                if (musicDocument.getUploadDate() != null) doc.addField("uploadDate", musicDocument.getUploadDate());
                // ⭐ 추가: Solr 스키마에 맞춰 createdAt, uploader_id, uploader_nickname 필드 추가 ⭐
                if (musicDocument.getCreatedAt() != null) doc.addField("created_at", musicDocument.getCreatedAt());
                if (musicDocument.getUploaderId() != null) doc.addField("uploader_id", musicDocument.getUploaderId());
                if (musicDocument.getUploaderNickname() != null) doc.addField("uploader_nickname", musicDocument.getUploaderNickname());
                return doc;
            })
            .collect(Collectors.toList());

        try {
            UpdateResponse response = solrClient.add(MUSIC_CORE_NAME, docs);
            solrClient.commit(MUSIC_CORE_NAME);
            log.info("Solr 문서 다중 색인 성공: {}개 문서, 상태={}", docs.size(), response.getStatus());
            return response.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            log.error("Solr 문서 다중 색인 실패: 에러: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Solr에서 음악 문서를 검색합니다. (Pageable 지원)
     * @param keyword 검색 키워드
     * @param pageable 페이지네이션 및 정렬 정보
     * @return 검색된 MusicDocument Page
     */
//    public Page<MusicResponseDTO> searchMusic(String keyword, Pageable pageable) {
//        log.info("MusicService - Solr 검색 호출: 키워드='{}', 페이지={}", keyword, pageable.getPageNumber());
//        // MusicSolrService의 searchMusicDocuments 메서드를 호출하여 MusicDocument 페이지를 얻습니다.
//        Page<SolrDocument> solrResultsPage = musicSolrService.searchMusicDocuments(keyword, pageable);
//
//        List<MusicResponseDTO> dtos = solrResultsPage.stream()
//                .map(solrDoc -> {
//                    MusicResponseDTO dto = new MusicResponseDTO();
//                    
//                    // ⭐⭐ 이 부분이 Line 353 부근이며, NumberFormatException을 방지하는 핵심 로직입니다. ⭐⭐
//                    // Solr 문서의 'id' 필드를 가져와 Long으로 변환합니다.
//                    // 'id' 필드가 'song001'과 같은 문자열이라면, NumberFormatException이 발생할 수 있으므로,
//                    // 안전하게 처리하는 로직입니다. Solr에 색인할 때 순수 숫자 ID를 'id' 필드에 넣는 것이 가장 좋습니다.
//                    // 만약 'song001'이 Solr의 문서 고유 ID이고, 실제 음악 ID는 'music_db_id'와 같은
//                    // 다른 필드에 저장되어 있다면 해당 필드를 사용해야 합니다.
//                    // 현재는 'id' 필드에서 숫자를 추출하거나, 파싱 오류 시 null로 처리하는 방어적인 코드를 추가합니다.
//                    Object solrDocId = solrDoc.getFieldValue("id"); 
    public Page<MusicDocument> searchMusicDocuments(String keyword, Pageable pageable) {
        log.info("MusicSolrService - 검색 호출: 키워드='{}', 페이지={}", keyword, pageable.getPageNumber());

        SolrQuery solrQuery = new SolrQuery();

        // 쿼리 문자열 구성: category와 uploader_nickname 필드 추가
        String escapedKeyword = ClientUtils.escapeQueryChars(keyword);
        String q = String.format("title:*%s* OR artist:*%s* OR album:*%s* OR lyrics:*%s* OR genre:*%s* OR tags:*%s* OR category:*%s* OR uploader_nickname:*%s*",
                escapedKeyword, escapedKeyword, escapedKeyword, escapedKeyword, escapedKeyword, escapedKeyword, escapedKeyword, escapedKeyword);
        solrQuery.setQuery(q);

        // 페이지네이션 설정
        solrQuery.setStart((int) pageable.getOffset());
        solrQuery.setRows(pageable.getPageSize());

        // 정렬 설정
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                String solrFieldName = order.getProperty();
                if ("releaseYear".equals(solrFieldName)) solrFieldName = "release_year";
                else if ("playCount".equals(solrFieldName)) solrFieldName = "play_count";
                else if ("musicUrl".equals(solrFieldName)) solrFieldName = "music_url";
                else if ("coverImagePath".equals(solrFieldName)) solrFieldName = "cover_image_path";
                else if ("uploadDate".equals(solrFieldName)) solrFieldName = "uploadDate";
                // ⭐ 추가: createdAt, uploaderId, uploaderNickname 정렬 매핑 ⭐
                else if ("createdAt".equals(solrFieldName)) solrFieldName = "created_at";
                else if ("uploaderId".equals(solrFieldName)) solrFieldName = "uploader_id";
                else if ("uploaderNickname".equals(solrFieldName)) solrFieldName = "uploader_nickname";


                solrQuery.addSort(solrFieldName,
                    order.getDirection().isAscending() ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
            });
        }
        if (!solrQuery.getSorts().iterator().hasNext()) {
             solrQuery.addSort("uploadDate", SolrQuery.ORDER.desc); // 기본 정렬 (업로드 날짜 최신순)
        }

        // 가져올 필드 목록: created_at, uploader_id, uploader_nickname 필드 추가
        solrQuery.setFields("id", "title", "artist", "album", "lyrics", "genre", "release_year",
                            "play_count", "category", "tags", "music_url", "duration",
                            "cover_image_path", "uploadDate", "created_at", "uploader_id", "uploader_nickname"); // ⭐ 필드 목록에 추가 ⭐

        List<MusicDocument> results = new ArrayList<>();
        long totalElements = 0;

        try {
            QueryResponse response = solrClient.query(MUSIC_CORE_NAME, solrQuery);
            SolrDocumentList documents = response.getResults();
            totalElements = documents.getNumFound();

            for (SolrDocument solrDoc : documents) {
                // ⭐ MusicDocument 생성자를 Builder 패턴으로 변경하여 안정성 향상 ⭐
                MusicDocument musicDoc = MusicDocument.builder()
                    .id((String) solrDoc.getFieldValue("id"))
//                    .title((String) solrDoc.getFieldValue("title"))
                    .title(getFirstStringValue(solrDoc.getFieldValue("title")))
                    .artist((List<String>) solrDoc.getFieldValue("artist"))
//                    .album((String) solrDoc.getFieldValue("album"))
                    .album(getFirstStringValue(solrDoc.getFieldValue("album")))
//                    .lyrics((String) solrDoc.getFieldValue("lyrics"))
                    .lyrics(getFirstStringValue(solrDoc.getFieldValue("lyrics")))
                    .genre((List<String>) solrDoc.getFieldValue("genre"))
                    .releaseYear((Integer) solrDoc.getFieldValue("release_year"))
                    .playCount((Long) solrDoc.getFieldValue("play_count"))
                    .category((List<String>) solrDoc.getFieldValue("category"))
                    .tags((List<String>) solrDoc.getFieldValue("tags"))
                    .musicUrl((String) solrDoc.getFieldValue("music_url"))
                    .duration((Integer) solrDoc.getFieldValue("duration"))
                    .coverImagePath((String) solrDoc.getFieldValue("cover_image_path"))
                    .uploadDate((Date) solrDoc.getFieldValue("uploadDate"))
                    .createdAt((Date) solrDoc.getFieldValue("created_at")) // ⭐ 추가: createdAt 필드 매핑 ⭐
                    .uploaderId((Long) solrDoc.getFieldValue("uploader_id")) // ⭐ 추가: uploaderId 필드 매핑 ⭐
                    .uploaderNickname((String) solrDoc.getFieldValue("uploader_nickname")) // ⭐ 추가: uploaderNickname 필드 매핑 ⭐
                    .build();
                results.add(musicDoc);
            }
            log.info("Solr 검색 실행 완료. 키워드='{}', 총 {}개 문서 중 {}개 조회됨.", keyword, totalElements, results.size());

        } catch (SolrServerException | IOException e) {
            log.error("Solr 검색 중 오류 발생: 키워드='{}', 에러: {}", keyword, e.getMessage(), e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        return new PageImpl<>(results, pageable, totalElements);
    }
    private String getFirstStringValue(Object value) {
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.isEmpty() ? null : String.valueOf(list.get(0));
        }
        return value != null ? value.toString() : null;
    }
    /**
     * 특정 ID의 문서를 삭제합니다.
     * @param id 삭제할 문서의 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteMusicDocument(String id) {
        try {
            UpdateResponse response = solrClient.deleteById(MUSIC_CORE_NAME, id);
            solrClient.commit(MUSIC_CORE_NAME);
            log.info("Solr 문서 삭제 성공: ID={}, 상태={}", id, response.getStatus());
            return response.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            log.error("Solr 문서 삭제 실패: ID={}, 에러: {}", id, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 모든 문서를 삭제합니다. (주의: 운영 환경에서는 신중하게 사용)
     * @return 삭제 성공 여부
     */
    public boolean deleteAllMusicDocuments() {
        try {
            UpdateResponse response = solrClient.deleteByQuery(MUSIC_CORE_NAME, "*:*");
            solrClient.commit(MUSIC_CORE_NAME);
            log.warn("Solr 모든 문서 삭제 요청 완료. 상태={}", response.getStatus());
            return response.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            log.error("Solr 모든 문서 삭제 실패: 에러: {}", e.getMessage(), e);
            return false;
        }
    }
}