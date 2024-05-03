package com.registration.book.book_network.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor @NoArgsConstructor
@Builder
public class FeedBackResponse {
    private Double rate;
    private String comment;
    private boolean ownFeedback;
}
