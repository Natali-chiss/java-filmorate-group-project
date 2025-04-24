package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class Review {
    private Long reviewId;
    @NotBlank(message = "Текст отзыва не может быть пустым")
    private String content;
    @NotNull(message = "Тип отзыва должен быть указан")
    private Boolean isPositive;
    @NotNull(message = "ID пользователя не может быть пустым")
    private Long userId;
    @NotNull(message = "ID фильма не может быть пустым")
    private Long filmId;
    private Integer useful = 0; // Рейтинг полезности, по умолчанию 0
}