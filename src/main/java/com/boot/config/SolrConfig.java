package com.boot.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrConfig {

    @Bean
    public SolrClient solrClient() {
        return new HttpSolrClient.Builder("http://localhost:8983/solr").build();
    }

    // ⭐ 참고: SolrTemplate은 사용하지 않기로 했으므로 여기에도 SolrTemplate 빈은 정의하지 않습니다.
    // SolrClient만 정의하고, 각 SolrService에서 이 SolrClient를 주입받아 사용합니다.
}