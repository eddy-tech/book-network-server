package com.registration.book.book_network.book.mapper;

import com.registration.book.book_network.book.dto.BookRequest;
import com.registration.book.book_network.book.dto.BookResponse;
import com.registration.book.book_network.book.dto.BorrowedBookResponse;
import com.registration.book.book_network.core.models.Book;
import com.registration.book.book_network.core.models.BookTransactionHistory;
import org.springframework.stereotype.Service;

import static com.registration.book.book_network.book.util.FileUtils.readFileFromLocation;

@Service
public class BookMapper {
    public Book toBook(BookRequest bookRequest){
        return Book.builder()
                .id(bookRequest.id())
                .title(bookRequest.title())
                .authorName(bookRequest.authorName())
                .isbn(bookRequest.isbn())
                .synopsis(bookRequest.synopsis())
                .archived(false)
                .shareable(bookRequest.shareable())
                .build();
    }

    public BookResponse toBookResponse(Book book) {
        return BookResponse.builder()
               .id(book.getId())
               .title(book.getTitle())
               .authorName(book.getAuthorName())
               .isbn(book.getIsbn())
               .owner(book.getOwner().getFullName())
               .rate(book.getRate())
               .synopsis(book.getSynopsis())
               .archived(book.isArchived())
               .shareable(book.isShareable())
               .cover(readFileFromLocation(book.getBookCoverPicture()))
               .build();
    }

    public BorrowedBookResponse toBorrowedBookResponse(BookTransactionHistory bookTransactionHistory) {
        return BorrowedBookResponse.builder()
               .id(bookTransactionHistory.getId())
               .title(bookTransactionHistory.getBook().getTitle())
               .authorName(bookTransactionHistory.getBook().getAuthorName())
               .isbn(bookTransactionHistory.getBook().getIsbn())
               .rate(bookTransactionHistory.getBook().getRate())
               .returned(bookTransactionHistory.isReturned())
               .returnApproved(bookTransactionHistory.isReturnApproved())
               .build();
    }
}
