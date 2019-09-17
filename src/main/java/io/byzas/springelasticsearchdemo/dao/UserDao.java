package io.byzas.springelasticsearchdemo.dao;

import io.byzas.springelasticsearchdemo.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class UserDao {
    @Value("${elasticsearch.index.name}")
    private String indexName;
    @Value("${elasticsearch.user.type}")
    private String userTypeName;
    @Autowired
    private ElasticsearchTemplate esTemplate;

    public Flux<User> findAllUsers() {
        SearchQuery findAllQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery()).build();
        return Flux.just(findAllQuery)
                .flatMap(query -> Flux.defer(() -> Flux.fromIterable(esTemplate.queryForList(query, User.class)))
                        .subscribeOn(Schedulers.parallel()))
                .timeout(Duration.ofSeconds(30));
    }

    public Mono<User> findUserById(String userId) {
        SearchQuery query = new NativeSearchQueryBuilder()
                .withFilter(QueryBuilders.matchQuery("_id", userId)).build();

        return Mono.defer(() -> {
            List<User> users = esTemplate.queryForList(query, User.class);
            return CollectionUtils.isEmpty(users) ? Mono.empty() : Mono.justOrEmpty(users.get(0));
        }).timeout(Duration.ofSeconds(30))
                .subscribeOn(Schedulers.parallel());
    }

    public Mono<User> add(User user) {
        return Mono.justOrEmpty(user)
                .flatMap(usr ->
                        Mono.defer(() -> {
                                    IndexQuery userQuery = new IndexQuery();
                                    userQuery.setIndexName(indexName);
                                    userQuery.setType(userTypeName);
                                    userQuery.setObject(usr);
                                    esTemplate.index(userQuery);
                                    esTemplate.refresh(indexName);

                                    return Mono.just(usr);
                                }
                        ))
                .timeout(Duration.ofSeconds(30))
                .subscribeOn(Schedulers.parallel())
                .doOnEach((x) -> log.info("User indexed {}", x));
    }

    public Mono<Map<String, String>> findAllUserSettings(String name) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("name", name)).build();

        return Mono.just(searchQuery)
                .flatMap(query -> Mono.defer(() -> Mono.justOrEmpty(esTemplate.queryForList(query, User.class))))
                .map(userList -> userList.get(0).getUserSettings())
                .timeout(Duration.ofSeconds(30))
                .subscribeOn(Schedulers.parallel());

    }

    public Mono<String> findUserSetting(String name, String key) {
        return findAllUserSettings(name)
                .filter(userSettingsMap -> userSettingsMap.containsKey(key))
                .map(setting -> setting.get(key));
    }

    public Mono<String> addUserSetting(String name, String key, String value) {
        return findAllUsers()
                .filter(user -> {
                    boolean result = user.getName().equals(name);
                    return result;
                })
                .flatMap(user ->
                        Mono.defer(() -> {
                            user.getUserSettings().put(key, value);

                            IndexQuery userQuery = new IndexQuery();
                            userQuery.setIndexName(indexName);
                            userQuery.setType(userTypeName);
                            userQuery.setId(user.getId());
                            userQuery.setObject(user);
                            esTemplate.index(userQuery);
                            esTemplate.refresh(indexName);
                            return Mono.just("Settings added..");
                        })
                )
                .elementAt(0)
                .timeout(Duration.ofSeconds(30))
                .subscribeOn(Schedulers.parallel());
    }
}
