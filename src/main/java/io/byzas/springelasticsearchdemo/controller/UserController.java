package io.byzas.springelasticsearchdemo.controller;

import io.byzas.springelasticsearchdemo.dao.UserDao;
import io.byzas.springelasticsearchdemo.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserDao userDao;

    @GetMapping("all")
    public Flux<User> getAllUsers() {
        return Flux.defer(() -> userDao.findAllUsers())
                .timeout(Duration.ofSeconds(20))
                .subscribeOn(Schedulers.parallel());
    }

    @GetMapping("/id/{userId}")
    public Mono<User> getUser(@PathVariable String userId) {
        return userDao.findUserById(userId);
    }

    @PostMapping(value = "/new")
    public Mono<User> addNewUsers(@RequestBody User user) {
        return userDao.add(user);
    }

    @GetMapping(value = "/settings/{name}")
    public Mono<Map<String, String>> getAllUserSettings(@PathVariable String name) {
        return userDao.findAllUserSettings(name);
    }

    @GetMapping(value = "/settings/{name}/{key}")
    public Mono<String> getUserSetting(
            @PathVariable String name, @PathVariable String key) {
        return userDao.findUserSetting(name, key);
    }

    @GetMapping(value = "/settings/{name}/{key}/{value}")
    public Mono<String> addUserSetting(
            @PathVariable String name,
            @PathVariable String key,
            @PathVariable String value) {
        return userDao.addUserSetting(name, key, value);
    }
}
