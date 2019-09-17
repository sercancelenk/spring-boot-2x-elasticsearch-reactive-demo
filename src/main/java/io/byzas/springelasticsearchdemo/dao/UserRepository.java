package io.byzas.springelasticsearchdemo.dao;

import io.byzas.springelasticsearchdemo.domain.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

import java.util.List;

public interface UserRepository extends ElasticsearchCrudRepository<User, String> {

}