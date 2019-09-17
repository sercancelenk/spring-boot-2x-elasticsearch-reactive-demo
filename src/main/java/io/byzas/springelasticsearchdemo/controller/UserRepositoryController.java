package io.byzas.springelasticsearchdemo.controller;

import io.byzas.springelasticsearchdemo.domain.User;
import io.byzas.springelasticsearchdemo.service.UserRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/repo")
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryController {
    private final UserRepositoryService userRepositoryService;

    @GetMapping("/all")
    public Flux<User> getAllUsers() {
        return userRepositoryService.getAllUsers();
    }

    @GetMapping("/id/{userId}")
    public Mono<User> getUser(@PathVariable String userId) {
        return userRepositoryService.getUserById(userId)
                .doOnSubscribe((val)-> log.info("Getting user with ID: {}", userId))
                .doOnSuccess((val) -> log.info("User with ID: {} is {}", userId, val));
    }

    @PostMapping(value = "/new")
    public Mono<User> addNewUsers(@RequestBody User user) {
        return userRepositoryService.addUser(user)
                .doOnSubscribe((val)-> log.info("Adding user : {}", user))
                .doOnSuccess((val) -> log.info("Added user : {}", user));
    }

    @GetMapping(value = "/settings/{userId}")
    public Object getAllUserSettings(@PathVariable String userId) {
        return userRepositoryService.getAllUserSettings(userId);
    }

    @GetMapping(value = "/settings/{userId}/{key}")
    public Mono<String> getUserSetting(
            @PathVariable String userId, @PathVariable String key) {
        return userRepositoryService.getUserSetting(userId, key);
    }

    @GetMapping(value = "/settings/{userId}/{key}/{value}")
    public Mono<String> addUserSetting(
            @PathVariable String userId,
            @PathVariable String key,
            @PathVariable String value) {
        return userRepositoryService.addUserSetting(userId, key, value);
    }

}
