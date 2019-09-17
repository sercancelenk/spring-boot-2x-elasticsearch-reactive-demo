package io.byzas.springelasticsearchdemo.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data @Builder
@Document(indexName = "user-index", type = "user", shards = 1, replicas = 0, refreshInterval = "-1")
public class User {

    private @Id String id;
    private String name;
    private @Field(type = FieldType.Date) Date creationDate = new Date();
    private Map<String, String> userSettings = new HashMap<>();

    public User(){}

    public User(String id, String name, Date creationDate, Map<String, String> userSettings) {
        this.id = id;
        this.name = name;
        this.creationDate = creationDate;
        this.userSettings = userSettings;
    }
}
