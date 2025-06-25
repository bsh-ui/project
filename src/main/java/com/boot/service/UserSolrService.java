package com.boot.service;

import com.boot.domain.UserDocument; // UserDocument 경로에 맞게 변경
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserSolrService {

    private final SolrClient solrClient;
    private static final String USERS_CORE_NAME = "users"; // Solr 사용자 코어 이름

    @Autowired
    public UserSolrService(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    /**
     * UserDocument 객체를 Solr에 색인합니다.
     * @param userDocument 색인할 UserDocument 객체
     * @return 색인 성공 여부
     */
    public boolean indexUserDocument(UserDocument userDocument) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", userDocument.getId());
        doc.addField("username", userDocument.getUsername());
        doc.addField("nickname", userDocument.getNickname());         // Solr 스키마 필드명 확인
        doc.addField("profile_description", userDocument.getProfileDescription()); // Solr 스키마 필드명 확인
        doc.addField("follower_count", userDocument.getFollowerCount()); // Solr 스키마 필드명 확인
        doc.addField("roles", userDocument.getRoles());             // List<String>은 multiValued 필드에 매핑

        try {
            UpdateResponse response = solrClient.add(USERS_CORE_NAME, doc);
            solrClient.commit(USERS_CORE_NAME);
            return response.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 여러 UserDocument 객체를 한 번에 Solr에 색인합니다.
     * @param userDocuments 색인할 UserDocument 객체 리스트
     * @return 색인 성공 여부
     */
    public boolean indexAllUserDocuments(List<UserDocument> userDocuments) {
        List<SolrInputDocument> docs = userDocuments.stream()
            .map(userDocument -> {
                SolrInputDocument doc = new SolrInputDocument();
                doc.addField("id", userDocument.getId());
                doc.addField("username", userDocument.getUsername());
                doc.addField("nickname", userDocument.getNickname());
                doc.addField("profile_description", userDocument.getProfileDescription());
                doc.addField("follower_count", userDocument.getFollowerCount());
                doc.addField("roles", userDocument.getRoles());
                return doc;
            })
            .collect(Collectors.toList());

        try {
            UpdateResponse response = solrClient.add(USERS_CORE_NAME, docs);
            solrClient.commit(USERS_CORE_NAME);
            return response.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Solr에서 사용자 문서를 검색합니다.
     * @param query 검색 쿼리 문자열
     * @param rows 검색 결과 수 제한
     * @return 검색된 UserDocument 리스트
     */
    public List<UserDocument> searchUserDocuments(String query, int rows) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery(query);
        solrQuery.setRows(rows);
        solrQuery.setFields("id", "username", "nickname", "profile_description", "follower_count", "roles"); // 필요한 필드만 가져오기

        List<UserDocument> results = new ArrayList<>();
        try {
            QueryResponse response = solrClient.query(USERS_CORE_NAME, solrQuery);
            SolrDocumentList documents = response.getResults();

            for (SolrDocument solrDoc : documents) {
                // SolrDocument에서 값을 읽어 UserDocument 객체로 수동 매핑
                UserDocument userDoc = UserDocument.builder()
                    .id((String) solrDoc.getFieldValue("id"))
                    .username((String) solrDoc.getFieldValue("username"))
                    .nickname((String) solrDoc.getFieldValue("nickname"))
                    .profileDescription((String) solrDoc.getFieldValue("profile_description"))
                    .followerCount((Integer) solrDoc.getFieldValue("follower_count"))
                    .roles((List<String>) solrDoc.getFieldValue("roles")) // List<String>으로 캐스팅
                    .build();
                results.add(userDoc);
            }
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * 특정 ID의 사용자 문서를 삭제합니다.
     * @param id 삭제할 문서의 ID
     * @return 삭제 성공 여부
     */
    public boolean deleteUserDocument(String id) {
        try {
            UpdateResponse response = solrClient.deleteById(USERS_CORE_NAME, id);
            solrClient.commit(USERS_CORE_NAME);
            return response.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 모든 사용자 문서를 삭제합니다. (주의: 운영 환경에서는 신중하게 사용)
     * @return 삭제 성공 여부
     */
    public boolean deleteAllUserDocuments() {
        try {
            UpdateResponse response = solrClient.deleteByQuery(USERS_CORE_NAME, "*:*");
            solrClient.commit(USERS_CORE_NAME);
            return response.getStatus() == 0;
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}