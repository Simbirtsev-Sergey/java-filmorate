package ru.yandex.practicum.filmorate.dto.review;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateReviewRequest {
    Long reviewId;
    @Size(
            max = 1000,
            message = "Максимальная длина описания — 1000 символов"
    )
    String content;
    Boolean isPositive;

    public boolean hasContent() {
        return !(content == null || content.isBlank());
    }

    public boolean hasIsPositive() {
        return isPositive != null;
    }
}