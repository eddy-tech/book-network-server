package com.registration.book.book_network.book.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

public record BookRequest(
        Integer id,
        @NotEmpty(message = "Title is mandatory")
        @NotNull(message = "Title is mandatory")
        String title,
        @NotEmpty(message = "Author name is mandatory")
        @NotNull(message = "Author name is mandatory")
        String authorName,
        @NotEmpty(message = "Synopsis is mandatory")
        @NotNull(message = "Synopsis is mandatory")
        String synopsis,
        @NotEmpty(message = "ISBN is mandatory")
        @NotNull(message = "ISBN is mandatory")
        String isbn,
        boolean shareable
) {
}
