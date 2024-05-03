package com.registration.book.book_network.book.repository;

import com.registration.book.book_network.core.models.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    @Query("""
        SELECT b
        FROM Book b
        WHERE b.archived = false\s
        AND b.shareable = true\s
        AND b.owner.id != :userId
    """)
    Page<Book> findAllDisplayableBooks(PageRequest pageable, Integer userId);
}
