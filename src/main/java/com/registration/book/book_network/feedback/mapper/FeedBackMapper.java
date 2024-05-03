package com.registration.book.book_network.feedback.mapper;

import com.registration.book.book_network.core.models.Book;
import com.registration.book.book_network.core.models.FeedBack;
import com.registration.book.book_network.feedback.dto.FeedBackResponse;
import com.registration.book.book_network.feedback.dto.FeedbackRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FeedBackMapper {
    public FeedBack toFeedBack(FeedbackRequest request) {
        return FeedBack.builder()
               .rate(request.note())
                .comment(request.comment())
                .book(Book.builder()
                        .id(request.bookId())
                        .archived(false)
                        .shareable(false)
                        .build())
               .build();
    }

    public FeedBackResponse toFeedbackResponse(FeedBack feedBack, Integer id) {
        return FeedBackResponse.builder()
               .rate(feedBack.getRate())
               .comment(feedBack.getComment())
               .ownFeedback(Objects.equals(feedBack.getCreatedBy(), id))
               .build();
    }
}
