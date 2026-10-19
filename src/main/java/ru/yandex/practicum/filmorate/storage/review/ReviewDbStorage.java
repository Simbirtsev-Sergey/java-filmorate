package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.BaseStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
public class ReviewDbStorage extends BaseStorage<Review> implements ReviewStorage {
    private static final String CREATE_REVIEW_QUERY = "INSERT INTO " +
            "Reviews(content, is_positive, user_id, film_id, useful)" +
            "VALUES(?, ?, ?, ?, ?)";
    private static final String FIND_REVIEW_BY_ID_QUERY = "SELECT * " +
            "FROM Reviews " +
            "WHERE review_id = ? " +
            "ORDER BY useful DESC ";
    private static final String DELETE_REVIEW_BY_ID_QUERY = "DELETE FROM Reviews " +
            "WHERE review_id = ?";
    private static final String UPDATE_REVIEW_QUERY = "UPDATE Reviews " +
            "SET content = ?, is_positive = ? " +
            "WHERE review_id = ?";
    private static final String ALL_REVIEW_QUERY = "SELECT * " +
            "FROM Reviews " +
            "ORDER BY useful DESC " +
            "LIMIT ?";
    private static final String ALL_REVIEW_BY_ID_QUERY = "SELECT * " +
            "FROM Reviews " +
            "WHERE film_id = ? " +
            "ORDER BY useful DESC " +
            "LIMIT ?";
    private static final String FIND_LIKE_REVIEWS_QUERY = "SELECT COUNT(*) " +
            "FROM Review_likes " +
            "WHERE user_id = ? AND review_id = ? AND is_like = ?";
    private static final String CREATE_REVIEW_LIKES_QUERY = "INSERT INTO Review_likes(user_id, review_id, is_like) " +
            "VALUES(?, ?, ?)";
    private static final String UPDATE_REVIEW_LIKES_QUERY = "UPDATE Review_likes " +
            "SET is_like = ? " +
            "WHERE user_id = ? AND review_id = ? ";
    private static final String UPDATE_USEFUL_QUERY = "UPDATE Reviews " +
            "SET useful = ? " +
            "WHERE review_id = ? ";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM Review_likes " +
            "WHERE review_id = ? AND user_id = ? ";

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Review create(final Review review) {
        long id = insert(CREATE_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful()
        );
        review.setReviewId(id);

        return review;
    }

    @Override
    public Optional<Review> getReviewById(final Long id) {
        return findOne(FIND_REVIEW_BY_ID_QUERY, id);
    }

    @Override
    public boolean delete(final Long id) {
        return delete(DELETE_REVIEW_BY_ID_QUERY, id);
    }

    @Override
    public Review update(Review updateReview) {
        update(UPDATE_REVIEW_QUERY,
                updateReview.getContent(),
                updateReview.getIsPositive(),
                updateReview.getReviewId()
        );
        return updateReview;
    }

    @Override
    public Collection<Review> getAllReviews(final Integer count) {
        return findMany(ALL_REVIEW_QUERY, count);
    }

    @Override
    public Collection<Review> getAllReviewsByIdFilms(final Long filmId, final Integer count) {
        return findMany(ALL_REVIEW_BY_ID_QUERY, filmId, count);
    }

    @Override
    public void addLikeReview(final Long id, final Long userId) {
        update(CREATE_REVIEW_LIKES_QUERY, userId, id, true);
    }

    @Override
    public void addDislikeReview(final Long id, final Long userId) {
        update(CREATE_REVIEW_LIKES_QUERY, userId, id, false);
    }

    @Override
    public boolean existsLikeOnReview(final Long id, final Long userId, boolean isLike) {
        return doesRowExist(FIND_LIKE_REVIEWS_QUERY, userId, id, isLike);
    }

    @Override
    public void replaceLikeAndDislike(final Long id, final Long userId, boolean isLike) {
        update(UPDATE_REVIEW_LIKES_QUERY, isLike, userId, id);
    }

    @Override
    public void updateUseful(final int useful, final Long reviewId) {
        update(UPDATE_USEFUL_QUERY, useful, reviewId);
    }

    @Override
    public boolean deleteLikeReview(final Long id, final Long userId) {
        return delete(DELETE_LIKE_QUERY, id, userId);
    }


}
