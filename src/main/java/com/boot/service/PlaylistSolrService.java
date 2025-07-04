package com.boot.service;

import com.boot.domain.PlaylistDocument; // PlaylistDocument 경로에 맞게 변경
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date; // java.util.Date를 사용합니다.
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaylistSolrService {

    private final SolrClient solrClient;
    private static final String PLAYLISTS_CORE_NAME = "playlists"; // Solr 플레이리스트 코어 이름

    @Autowired
    public PlaylistSolrService(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    /**
     * PlaylistDocument 객체를 Solr에 색인합니다.
     * @param playlistDocument 색인할 PlaylistDocument 객체
     * @return 색인 성공 여부
     */
    public boolean indexPlaylistDocument(PlaylistDocument playlistDocument) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", playlistDocument.getId());
        doc.addField("title", playlistDocument.getTitle());
        doc.addField("description", playlistDocument.getDescription());
        doc.addField("creator_id", playlistDocument.getCreatorId()); // Solr 스키마 필드명 확인
        doc.addField("created_at", playlistDocument.getCreatedAt()); // Solr 스키마 필드명 확인
        doc.addField("is_public", playlistDocument.getIsPublic());
        doc.addField("music_ids", playlistDocument.getMusicIds()); // List<String>은 multiValued 필드에 매핑

        try {
            UpdateResponse response = solrClient.add(PLAYLISTS_CORE_NAME, doc);
            solrClient.commit(PLAYLISTS_CORE_NAME);
            return response.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 여러 PlaylistDocument 객체를 한 번에 Solr에 색인합니다.
     * @param playlistDocuments 색인할 PlaylistDocument 객체 리스트
     * @return 색인 성공 여부
     */
    public boolean indexAllPlaylistDocuments(List<PlaylistDocument> playlistDocuments) {
        List<SolrInputDocument> docs = playlistDocuments.stream()
            .map(playlistDocument -> {
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", playlistDocument.getId());
                doc.addField("title", playlistDocument.getTitle());
                doc.addField("description", playlistDocument.getDescription());
                doc.addField("creator_id", playlistDocument.getCreatorId());
                doc.addField("created_at", playlistDocument.getCreatedAt());
                doc.addField("is_public", playlistDocument.getIsPublic());
                doc.addField("music_ids", playlistDocument.getMusicIds());
                return doc;
            })
            .collect(Collectors.toList());

        try {
            UpdateResponse response = solrClient.add(PLAYLISTS_CORE_NAME, docs);
            solrClient.commit(PLAYLISTS_CORE_NAME);
            return response.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Solr에서 플레이리스트 문서를 검색합니다.
     * @param query 검색 쿼리 문자열
     * @param rows 검색 결과 수 제한
     * @return 검색된 PlaylistDocument 리스트
     */
    public List<PlaylistDocument> searchPlaylistDocuments(String query, int rows) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(query);
        solrQuery.setRows(rows);
        solrQuery.setFields("id", "title", "description", "creator_id", "created_at", "is_public", "music_ids"); // 필요한 필드만 가져오기

        List<PlaylistDocument> results = new ArrayList<>();
        try {
            QueryResponse response = solrClient.query(PLAYLISTS_CORE_NAME, solrQuery);
            SolrDocumentList documents = response.getResults();

            for (SolrDocument solrDoc : documents) {
                // SolrDocument에서 값을 읽어 PlaylistDocument 객체로 수동 매핑
                PlaylistDocument playlistDoc = PlaylistDocument.builder()
                    .id((String) solrDoc.getFieldValue("id"))
                    .title((String) solrDoc.getFieldValue("title"))
                    .description((String) solrDoc.getFieldValue("description"))
                    .creatorId((String) solrDoc.getFieldValue("creator_id")) // String으로 캐스팅
                    .createdAt((Date) solrDoc.getFieldValue("created_at"))   // Date로 캐스팅
                    .isPublic((Boolean) solrDoc.getFieldValue("is_public"))  // Boolean으로 캐스팅
                    .musicIds((List<String>) solrDoc.getFieldValue("music_ids")) // List<String>으로 캐스팅
                    .build();
                results.add(playlistDoc);
            }
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * 특정 ID의 플레이리스트 문서를 삭제합니다.
     * @param id 삭제할 문서의 ID
     * @return 삭제 성공 여부
     */
    public boolean deletePlaylistDocument(String id) {
        try {
            UpdateResponse response = solrClient.deleteById(PLAYLISTS_CORE_NAME, id);
            solrClient.commit(PLAYLISTS_CORE_NAME);
            return response.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 모든 플레이리스트 문서를 삭제합니다. (주의: 운영 환경에서는 신중하게 사용)
     * @return 삭제 성공 여부
     */
    public boolean deleteAllPlaylistDocuments() {
        try {
            UpdateResponse response = solrClient.deleteByQuery(PLAYLISTS_CORE_NAME, "*:*");
            solrClient.commit(PLAYLISTS_CORE_NAME);
            return response.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}