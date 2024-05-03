package com.registration.book.book_network.history_book.repository;

import com.registration.book.book_network.core.models.BookTransactionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BookTransactionHistoryRepository extends JpaRepository<BookTransactionHistory, Integer> {
    @Query("""
        SELECT history
        FROM BookTransactionHistory history
        WHERE history.user.id =: userId
    """)
    Page<BookTransactionHistory> findAllBorrowedBooks(PageRequest pageable, Integer userId);

    @Query("""
        SELECT history
        FROM BookTransactionHistory history
        WHERE history.book.owner.id =: userId
    """)
    Page<BookTransactionHistory> findAllReturnedBooks(PageRequest pageable, Integer userId);

    @Query("""
        SELECT (
        COUNT(*) > 0) AS isBorrowed
        FROM BookTransactionHistory history
        WHERE history.book.id =: bookId
        AND history.user.id =: userId
        AND history.returnApproved = false
    """)
    boolean isAlreadyBorrowedByUser(Integer bookId, Integer userId);

    @Query("""
        SELECT transaction
        FROM BookTransactionHistory transaction
        WHERE transaction.book.id =: bookId
        AND transaction.user.id =: userId
        AND transaction.returned = false
        AND transaction.returnApproved = false
        """)
    Optional<BookTransactionHistory> findByBookIdAndUserId(Integer bookId, Integer id);

    @Query("""
        SELECT transaction
        FROM BookTransactionHistory transaction
        WHERE transaction.book.id =: bookId
        AND transaction.book.owner.id =: userId
        AND transaction.returned = true
        AND transaction.returnApproved = false
        """)
    Optional<BookTransactionHistory> findByBookIdAndOwnerId(Integer bookId, Integer id);
}
