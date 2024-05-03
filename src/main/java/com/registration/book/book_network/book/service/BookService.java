package com.registration.book.book_network.book.service;

import com.registration.book.book_network.book.dto.BookRequest;
import com.registration.book.book_network.book.dto.BookResponse;
import com.registration.book.book_network.book.dto.BorrowedBookResponse;
import com.registration.book.book_network.book.mapper.BookMapper;
import com.registration.book.book_network.book.repository.BookRepository;
import com.registration.book.book_network.core.models.Book;
import com.registration.book.book_network.core.models.BookTransactionHistory;
import com.registration.book.book_network.core.models.User;
import com.registration.book.book_network.core.models.common.PageResponse;
import com.registration.book.book_network.history_book.repository.BookTransactionHistoryRepository;
import com.registration.book.core.exception.OperationNotPermittedException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

import static com.registration.book.book_network.book.repository.BookSpecification.withOwnerId;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository bookTransactionHistoryRepository;
    private final BookMapper bookMapper;
    private final FileStorageService fileStorageService;

    private User getCurrentUser(Authentication connectedUser) {
        return ((User) connectedUser.getPrincipal());
    }
    public Integer save(BookRequest bookRequest, Authentication connectedUser) {
        var user = this.getCurrentUser(connectedUser);
        var book = bookMapper.toBook(bookRequest);
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }

    public BookResponse getBookById(Integer bookId) {
        return bookRepository.findById(bookId)
                .map(bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("No Book has been found with the ID:: " + bookId));
    }

    private Book findBookById(Integer bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No Book has been found with the ID:: " + bookId));
    }

    public PageResponse<BookResponse> findAllBooks(Integer page, Integer size, Authentication connectedUser) {
        var user = this.getCurrentUser(connectedUser);
        var pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        var books = bookRepository.findAllDisplayableBooks(pageable, user.getId());

        return pageResponse(books);
    }

    public PageResponse<BookResponse> findAllBooksByOwner(Integer page, Integer size, Authentication connectedUser) {
        var user = this.getCurrentUser(connectedUser);
        var pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        var books = bookRepository.findAll(withOwnerId(user.getId()), pageable);

        return pageResponse(books);
    }

    private PageResponse<BorrowedBookResponse> pageBorrowedResponse(Page<BookTransactionHistory> allBorrowedBooks) {
        var bookResponse = allBorrowedBooks.stream()
                .map(bookMapper::toBorrowedBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                allBorrowedBooks.getNumber(),
                allBorrowedBooks.getSize(),
                allBorrowedBooks.getTotalElements(),
                allBorrowedBooks.getTotalPages(),
                allBorrowedBooks.isFirst(),
                allBorrowedBooks.isLast()
        );
    }

    private PageResponse<BookResponse> pageResponse(Page<Book> books) {
        var bookResponse = books.stream()
                .map(bookMapper::toBookResponse)
                .toList();

        return new PageResponse<>(
                bookResponse,
                books.getNumber(),
                books.getSize(),
                books.getTotalElements(),
                books.getTotalPages(),
                books.isFirst(),
                books.isLast()
        );
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(Integer page, Integer size,
                                                                   Authentication connectedUser) {
        var user = this.getCurrentUser(connectedUser);
        var pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        var allBorrowedBooks = bookTransactionHistoryRepository.findAllBorrowedBooks(pageable, user.getId());

        return pageBorrowedResponse(allBorrowedBooks);
    }


    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(Integer page,
                                                                   Integer size, Authentication connectedUser) {
        var user = this.getCurrentUser(connectedUser);
        var pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        var allBorrowedBooks = bookTransactionHistoryRepository.findAllReturnedBooks(pageable, user.getId());

        return pageBorrowedResponse(allBorrowedBooks);
    }

    public Integer updateShareableStatus(Integer bookId, Authentication connectedUser) {
        var book = this.manageStatus(bookId, connectedUser);
        book.setShareable(!book.isShareable());

        return bookRepository.save(book).getId();
    }

    public Integer updateArchivedStatus(Integer bookId, Authentication connectedUser) {
        var book = this.manageStatus(bookId, connectedUser);
        book.setArchived(!book.isArchived());
        return bookRepository.save(book).getId();
    }

    private Book manageStatus(Integer bookId, Authentication connectedUser) {
        var book = this.findBookById(bookId);
        var user = this.getCurrentUser(connectedUser);

        // Just the user who has been created book can share or archive book
        if(!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others books archived status");
        }

        return book;
    }

    public Integer borrowBook(Integer bookId, Authentication connectedUser) {
        var user = this.checkedBookAndUser(bookId, connectedUser);
        var book = this.findBookById(bookId);

        final boolean isAlreadyBorrowed = bookTransactionHistoryRepository.isAlreadyBorrowedByUser(
                bookId,
                user.getId()
        );

        if(isAlreadyBorrowed) {
            throw new OperationNotPermittedException("The requested book is already borrowed");
        }

        var bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returnApproved(false)
                .returned(false)
                .build();

        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer returnBorrowBook(Integer bookId, Authentication connectedUser) {
        var user = this.checkedBookAndUser(bookId, connectedUser);

        var bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(()-> new OperationNotPermittedException("You did not have permission to borrow this book"));
        bookTransactionHistory.setReturned(true);

        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Integer approveReturnBorrowedBook(Integer bookId, Authentication connectedUser) {
        var book = this.findBookById(bookId);

        if(book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived " +
                    "or not shareable");
        }

        var user = this.getCurrentUser(connectedUser);
        if(!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot return a book that you don't own'");
        }

        var bookTransactionHistory = bookTransactionHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(()-> new OperationNotPermittedException("The book is not returned yet. You can't " +
                        "approve its return'"));
        bookTransactionHistory.setReturnApproved(true);

        return bookTransactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    private User checkedBookAndUser(Integer bookId, Authentication connectedUser) {
        var book = this.findBookById(bookId);
        if(book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("The requested book cannot be borrowed since it is archived " +
                    "or not shareable");
        }

        var user = this.getCurrentUser(connectedUser);
        if(Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own book");
        }

        return user;
    }

    public void uploadCoverPicture(Integer bookId, MultipartFile file, Authentication connectedUser) {
        var book = findBookById(bookId);
        var user = getCurrentUser(connectedUser);
        var bookCover = fileStorageService.saveFile(file, user.getId());
        book.setBookCoverPicture(bookCover);
        bookRepository.save(book);
    }
}
