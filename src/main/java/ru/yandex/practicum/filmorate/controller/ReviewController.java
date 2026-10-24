package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.review.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.review.ReviewDto;
import ru.yandex.practicum.filmorate.dto.review.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private static final String urlLike = "/{id}/like/{userId}";
    private static final String urlDislike = "/{id}/dislike/{userId}";

    @Autowired
    public ReviewController(final ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ReviewDto create(@Valid @RequestBody final NewReviewRequest newReviewRequest) {
        return reviewService.create(newReviewRequest);
    }

    @GetMapping("/{id}")
    public ReviewDto getReviewById(@PathVariable final Long id) {
        return reviewService.getReviewById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteReviewById(@PathVariable final Long id) {
        reviewService.deleteReviewById(id);
    }

    @PutMapping
    public ReviewDto update(@Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.update(request);
    }

    @GetMapping
    public Collection<ReviewDto> getReviewByIdFilms(@RequestParam(required = false) final Long filmId,
                                                    @RequestParam(defaultValue = "10") final Integer count) {
        return reviewService.getReviewByIdFilms(filmId, count);
    }

    @PutMapping(urlLike)
    public void userAddLikesReview(@PathVariable final Long id,
                                   @PathVariable final Long userId) {
        reviewService.addLikeReview(id, userId);
    }

    @PutMapping(urlDislike)
    public void userAddDislikesReview(@PathVariable final Long id,
                                      @PathVariable final Long userId) {
        reviewService.addDislikeReview(id, userId);
    }

    @DeleteMapping(urlLike)
    public void userDeleteLikesReview(@PathVariable final Long id,
                                      @PathVariable final Long userId) {
        reviewService.deleteLikeReview(id, userId);
    }

    @DeleteMapping(urlDislike)
    public void userDeleteDislikesReview(@PathVariable final Long id,
                                         @PathVariable final Long userId) {
        reviewService.deleteDislikeReview(id, userId);
    }
}