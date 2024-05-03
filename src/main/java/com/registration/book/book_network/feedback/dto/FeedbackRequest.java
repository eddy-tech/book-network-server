package com.registration.book.book_network.feedback.dto;

import jakarta.validation.constraints.*;

public record FeedbackRequest(
        @Positive(message = "The rate of feedback is required")
        @Min(value = 0, message = "The rate of feedback must be at least 0")
        @Max(value = 5, message = "The rate of feedback must not be greater than 5")
        Double note,
        @NotNull(message = "The comment is required")
        @NotEmpty(message = "The comment is required")
        @NotBlank(message = "The comment is required")
        String comment,
        @NotNull(message = "We have to know the rate book")
        Integer bookId
) {
}
