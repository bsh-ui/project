package com.boot.domain; // 실제 패키지명으로 변경

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDocument {

    private String id; // Solr의 id (Long -> String)
    private String username;
    private String nickname; // Solr 스키마의 'display_name' 필드
    private String profileDescription;
    private Integer followerCount; // Solr 스키마의 'follower_count' 필드
    private List<String> roles;
}