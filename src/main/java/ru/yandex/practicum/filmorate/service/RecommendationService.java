package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FilmResponse;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final JdbcTemplate jdbcTemplate;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    // Найти пользователя с максимальным количеством общих лайков
    private final static String SQL_FIND_SIMILAR_USER = """
            SELECT fl2.user_id, COUNT(*) as common_likes
            FROM film_likes fl1
            JOIN film_likes fl2 ON fl1.film_id = fl2.film_id
            WHERE fl1.user_id = ? AND fl2.user_id != ?
            GROUP BY fl2.user_id
            ORDER BY common_likes DESC
            LIMIT 1
        """;
    // Найти фильмы, которые лайкнул похожий пользователь, но не лайкнул текущий
    private final static String SQL_RECOMMENDATIONS = """
            SELECT fl.film_id
            FROM film_likes fl
            WHERE fl.user_id = ?
            AND fl.film_id NOT IN (
                SELECT film_id
                FROM film_likes
                WHERE user_id = ?
            )
        """;

    public Collection<FilmResponse> getRecommendations(Long userId) {
        // Проверка существования пользователя
        if (userStorage.findById(userId) == null) {
            throw new NotFoundException("User with id " + userId + " not found");
        }

        Long similarUserId = jdbcTemplate.query(SQL_FIND_SIMILAR_USER, ps -> {
            ps.setLong(1, userId);
            ps.setLong(2, userId);
        }, rs -> rs.next() ? rs.getLong("user_id") : null);

        if (similarUserId == null) {
            return List.of();
        }
        
        List<Long> recommendedFilmIds = jdbcTemplate.query(SQL_RECOMMENDATIONS, ps -> {
            ps.setLong(1, similarUserId);
            ps.setLong(2, userId);
        }, (rs, rowNum) -> rs.getLong("film_id"));

        // Получить объекты фильмов
        return recommendedFilmIds.stream()
                .map(filmStorage::findById)
                .filter(film -> film != null)
                .collect(Collectors.toList());
    }
}