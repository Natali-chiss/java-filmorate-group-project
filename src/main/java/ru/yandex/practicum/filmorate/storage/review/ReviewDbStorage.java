package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"review_id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setLong(3, review.getUserId());
            stmt.setLong(4, review.getFilmId());
            stmt.setInt(5, review.getUseful());
            return stmt;
        }, keyHolder);
        review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ?, useful = ? WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getUseful(), review.getReviewId());
        return review;
    }

    @Override
    public void delete(Long reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, reviewId);
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        String sql = "SELECT review_id, content, is_positive, user_id, film_id, useful FROM reviews WHERE review_id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, this::mapRowToReview, reviewId);
        return reviews.isEmpty() ? Optional.empty() : Optional.of(reviews.get(0));
    }

    @Override
    public List<Review> findByFilmId(Long filmId, int count) {
        String sql = "SELECT review_id, content, is_positive, user_id, film_id, useful FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
    }

    @Override
    public List<Review> findAll(int count) {
        String sql = "SELECT review_id, content, is_positive, user_id, film_id, useful FROM reviews ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, count);
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        String sql = "MERGE INTO review_ratings (review_id, user_id, rating) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, reviewId, userId, 1);
        updateUseful(reviewId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        String sql = "MERGE INTO review_ratings (review_id, user_id, rating) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, reviewId, userId, -1);
        updateUseful(reviewId);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_ratings WHERE review_id = ? AND user_id = ? AND rating = ?";
        jdbcTemplate.update(sql, reviewId, userId, 1);
        updateUseful(reviewId);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        String sql = "DELETE FROM review_ratings WHERE review_id = ? AND user_id = ? AND rating = ?";
        jdbcTemplate.update(sql, reviewId, userId, -1);
        updateUseful(reviewId);
    }

    private void updateUseful(Long reviewId) {
        String sql = "UPDATE reviews SET useful = (SELECT COALESCE(SUM(rating), 0) FROM review_ratings WHERE review_id = ?) WHERE review_id = ?";
        jdbcTemplate.update(sql, reviewId, reviewId);
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getLong("review_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getLong("user_id"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    }
}