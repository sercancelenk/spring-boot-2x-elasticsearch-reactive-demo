package io.byzas.springelasticsearchdemo.service;

import io.byzas.springelasticsearchdemo.dao.UserRepository;
import io.byzas.springelasticsearchdemo.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryService {
    private final UserRepository userRepository;

    public Flux<User> getAllUsers() {
        return Flux.defer(() -> Flux.fromIterable(userRepository.findAll())).subscribeOn(Schedulers.parallel()).timeout(Duration.ofSeconds(30));
    }

    public Mono<User> getUserById(String userId) {
        return Mono.defer(() -> Mono.justOrEmpty(userRepository.findById(userId))).subscribeOn(Schedulers.parallel()).timeout(Duration.ofSeconds(30));
    }

    public Mono<User> addUser(User user) {
        return Mono.defer(() -> Mono.justOrEmpty(userRepository.save(user))).subscribeOn(Schedulers.parallel()).timeout(Duration.ofSeconds(30));
    }

    public Mono<Map<String, String>> getAllUserSettings(String userId) {
        return Mono.justOrEmpty(userId)
                .switchIfEmpty(Mono.error(() -> new RuntimeException("User id can not be empty")))
                .flatMap(usrId -> Mono.defer(() -> Mono.justOrEmpty(userRepository.findById(userId))).subscribeOn(Schedulers.parallel()).timeout(Duration.ofSeconds(30)))
                .switchIfEmpty(Mono.error(() -> new RuntimeException("User not found")))
                .map(user -> user.getUserSettings());
    }

    public Mono<String> getUserSetting(String userId, String key) {
        return getAllUserSettings(userId)
                .map(settings -> settings.get(key));
    }

    public Mono<String> addUserSetting(String userId, String key, String val) {
        return getUserById(userId)
                .flatMap(user -> Mono.defer(() -> {
                    user.getUserSettings().put(key, val);
                    return Mono.justOrEmpty(userRepository.save(user));
                }).subscribeOn(Schedulers.parallel()).timeout(Duration.ofSeconds(30))).switchIfEmpty(Mono.just(new User()))
                .map(userEntity -> {
                    if (Objects.isNull(userEntity.getId())) return "User not found";
                    return "Key added";
                });
    }


}
