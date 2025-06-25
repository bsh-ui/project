package com.boot.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PlaylistDocument {

    @Id
    @Field
    private String id;

    @Field
    private String title;

    @Field
    private String description;

    @Field("creator_id")
    private String creatorId;

    // ⭐ 추가: creatorNickname 필드 ⭐
    @Field("creator_nickname")
    private String creatorNickname;

    @Field("created_at")
    private Date createdAt;

     // ⭐ 추가: updatedAt 필드 ⭐
    @Field("updated_at")
    private Date updatedAt;

    @Field("is_public")
    private Boolean isPublic;

    @Field("music_ids")
    private List<String> musicIds;
}