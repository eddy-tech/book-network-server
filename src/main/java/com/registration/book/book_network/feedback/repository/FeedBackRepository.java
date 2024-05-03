package com.registration.book.book_network.feedback.repository;

import com.registration.book.book_network.core.models.FeedBack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeedBackRepository extends JpaRepository<FeedBack, Integer> {
    @Query("""
        SELECT f
        FROM FeedBack f
        WHERE f.book.id = :bookId
    """)
    Page<FeedBack> findAllByBookId(Integer bookId, PageRequest pageable);
}
