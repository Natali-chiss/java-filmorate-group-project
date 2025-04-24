package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private static final String REVIEW_NOT_FOUND = "Review not found";
    private static final String FILM_NOT_FOUND = "Film not found";
    private static final String USER_NOT_FOUND = "User not found";

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review create(Review review) {
        validateReview(review);
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        validateReview(review);
        if (reviewStorage.findById(review.getReviewId()).isEmpty()) {
            throw new NotFoundException(REVIEW_NOT_FOUND);
        }
        return reviewStorage.update(review);
    }

    public void delete(Long reviewId) {
        if (reviewStorage.findById(reviewId).isEmpty()) {
            throw new NotFoundException(REVIEW_NOT_FOUND);
        }
        reviewStorage.delete(reviewId);
    }

    public Review findById(Long reviewId) {
        return reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException(REVIEW_NOT_FOUND));
    }

    public List<Review> findByFilmId(Long filmId, int count) {
        if (filmId != null && filmStorage.findById(filmId) == null) {
            throw new NotFoundException(FILM_NOT_FOUND);
        }
        return filmId != null ? reviewStorage.findByFilmId(filmId, count) : reviewStorage.findAll(count);
    }

    public void addLike(Long reviewId, Long userId) {
        validateUserAndReview(reviewId, userId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateUserAndReview(reviewId, userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        validateUserAndReview(reviewId, userId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        validateUserAndReview(reviewId, userId);
        reviewStorage.removeDislike(reviewId, userId);
    }

    private void validateReview(Review review) {
        if (userStorage.findById(review.getUserId()) == null) {
            throw new NotFoundException(USER_NOT_FOUND);
        }
        if (filmStorage.findById(review.getFilmId()) == null) {
            throw new NotFoundException(FILM_NOT_FOUND);
        }
    }

    private void validateUserAndReview(Long reviewId, Long userId) {
        if (userStorage.findById(userId) == null) {
            throw new NotFoundException(USER_NOT_FOUND);
        }
        if (reviewStorage.findById(reviewId).isEmpty()) {
            throw new NotFoundException(REVIEW_NOT_FOUND);
        }
    }
}