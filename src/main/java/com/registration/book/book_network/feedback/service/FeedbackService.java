package com.registration.book.book_network.feedback.service;

import com.registration.book.book_network.book.repository.BookRepository;
import com.registration.book.book_network.core.models.User;
import com.registration.book.book_network.core.models.common.PageResponse;
import com.registration.book.book_network.feedback.dto.FeedBackResponse;
import com.registration.book.book_network.feedback.dto.FeedbackRequest;
import com.registration.book.book_network.feedback.mapper.FeedBackMapper;
import com.registration.book.book_network.feedback.repository.FeedBackRepository;
import com.registration.book.core.exception.OperationNotPermittedException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedbackService {
    private final BookRepository bookRepository;
    private final FeedBackRepository feedbackRepository;
    private final FeedBackMapper feedBackMapper;

    private User getCurrentUser(Authentication connectedUser) {
        return ((User) connectedUser.getPrincipal());
    }
    public Integer save(FeedbackRequest request, Authentication connectedUser) {
        var book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new EntityNotFoundException("No Book has been found with the ID:: " + request.bookId()));

        if(book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("You cannot give a feedback for an archived or not shareable book");
        }

        var user = getCurrentUser(connectedUser);
        if(Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot give a feedback for your own book");
        }

        var saveFeedback = feedBackMapper.toFeedBack(request);
        return feedbackRepository.save(saveFeedback).getId();
    }

    public PageResponse<FeedBackResponse> getAllFeedbackByBook(Integer bookId, int page, int size,
                                                               Authentication connectedUser) {
        var pageable = PageRequest.of(page, size);
        var user = getCurrentUser(connectedUser);
        var feedbacks = feedbackRepository.findAllByBookId(bookId, pageable);
        var feedbackResponses = feedbacks.stream()
                .map(feedBack -> feedBackMapper.toFeedbackResponse(feedBack, user.getId()))
                .toList();

        return new PageResponse<>(
                feedbackResponses,
                feedbacks.getNumber(),
                feedbacks.getSize(),
                feedbacks.getTotalElements(),
                feedbacks.getTotalPages(),
                feedbacks.isFirst(),
                feedbacks.isLast()
        );
    }
}
