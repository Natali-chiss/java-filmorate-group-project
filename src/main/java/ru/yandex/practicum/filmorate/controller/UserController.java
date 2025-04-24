package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.FilmResponse;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.RecommendationService;
import ru.yandex.practicum.filmorate.service.UserServiceImpl;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private static final String USER_ID_PATH = "/{id}";
    private static final String FRIENDS_PATH = USER_ID_PATH + "/friends";
    private static final String FRIEND_ID_PATH = FRIENDS_PATH + "/{friendId}";
    private static final String COMMON_FRIENDS_PATH = USER_ID_PATH + "/friends/common/{otherId}";
    private static final String RECOMMENDATIONS_PATH = USER_ID_PATH + "/recommendations";

    private final UserStorage userStorage;
    private final UserServiceImpl userServiceImpl;
    private final RecommendationService recommendationService;

    @GetMapping
    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    @GetMapping(USER_ID_PATH)
    public User findById(@PathVariable("id") Long id) {
        return userStorage.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        return userStorage.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        return userStorage.update(newUser);
    }

    @PutMapping(FRIEND_ID_PATH)
    public User addFriend(@Valid @PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        return userServiceImpl.addFriend(id, friendId);
    }

    @DeleteMapping(FRIEND_ID_PATH)
    public User delFriend(@Valid @PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        return userServiceImpl.delFriend(id, friendId);
    }

    @GetMapping(COMMON_FRIENDS_PATH)
    public Set<User> findJointFriends(@Valid @PathVariable("id") Long id, @PathVariable("otherId") Long otherId) {
        return userServiceImpl.findJointFriends(id, otherId);
    }

    @GetMapping(FRIENDS_PATH)
    public Set<User> findAllFriends(@Valid @PathVariable("id") Long id) {
        return userServiceImpl.findAllFriends(id);
    }

    @GetMapping(RECOMMENDATIONS_PATH)
    public Collection<FilmResponse> getRecommendations(@PathVariable("id") Long id) {
        return recommendationService.getRecommendations(id);
    }
}