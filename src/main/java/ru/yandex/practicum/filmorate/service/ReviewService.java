package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDao;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewDao reviewDao;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review create(Review review) {
        validateReview(review);
        return reviewDao.create(review);
    }

    public Review update(Review review) {
        validateReview(review);
        if (reviewDao.findById(review.getReviewId()).isEmpty()) {
            throw new NotFoundException("Review not found");
        }
        return reviewDao.update(review);
    }

    public void delete(Long reviewId) {
        if (reviewDao.findById(reviewId).isEmpty()) {
            throw new NotFoundException("Review not found");
        }
        reviewDao.delete(reviewId);
    }

    public Review findById(Long reviewId) {
        return reviewDao.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
    }

    public List<Review> findByFilmId(Long filmId, int count) {
        if (filmId != null && filmStorage.findById(filmId) == null) {
            throw new NotFoundException("Film not found");
        }
        return filmId != null ? reviewDao.findByFilmId(filmId, count) : reviewDao.findAll(count);
    }

    public void addLike(Long reviewId, Long userId) {
        validateUserAndReview(reviewId, userId);
        reviewDao.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateUserAndReview(reviewId, userId);
        reviewDao.addDislike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        validateUserAndReview(reviewId, userId);
        reviewDao.removeLike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        validateUserAndReview(reviewId, userId);
        reviewDao.removeDislike(reviewId, userId);
    }

    private void validateReview(Review review) {
        if (userStorage.findById(review.getUserId()) == null) {
            throw new NotFoundException("User not found");
        }
        if (filmStorage.findById(review.getFilmId()) == null) {
            throw new NotFoundException("Film not found");
        }
    }

    private void validateUserAndReview(Long reviewId, Long userId) {
        if (userStorage.findById(userId) == null) {
            throw new NotFoundException("User not found");
        }
        if (reviewDao.findById(reviewId).isEmpty()) {
            throw new NotFoundException("Review not found");
        }
    }
}