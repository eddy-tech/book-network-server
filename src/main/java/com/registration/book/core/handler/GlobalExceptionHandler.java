package com.registration.book.core.handler;

import com.registration.book.core.exception.OperationNotPermittedException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;

import static com.registration.book.core.handler.BusinessErrorCodes.*;
import static com.registration.book.core.handler.BusinessErrorCodes.BAD_CREDENTIALS;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(LockedException exp) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse
                               .builder()
                               .businessErrorCode(ACCOUNT_LOCKED.getCode())
                               .businessErrorDescription(ACCOUNT_LOCKED.getDescription())
                               .error(exp.getMessage())
                               .build()
                );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleException(DisabledException exp) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse
                               .builder()
                               .businessErrorCode(ACCOUNT_DISABLED.getCode())
                               .businessErrorDescription(ACCOUNT_DISABLED.getDescription())
                               .error(exp.getMessage())
                               .build()
                );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException(BadCredentialsException exp) {
        return ResponseEntity
                .status(UNAUTHORIZED)
                .body(
                        ExceptionResponse
                               .builder()
                               .businessErrorCode(BAD_CREDENTIALS.getCode())
                               .businessErrorDescription(BAD_CREDENTIALS.getDescription())
                               .error(exp.getMessage())
                               .build()
                );
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleException(MessagingException exp) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse
                               .builder()
                               .error(exp.getMessage())
                               .build()
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(MethodArgumentNotValidException exp) {
        var errors = new HashSet<String>();
        exp.getBindingResult().getAllErrors()
                .forEach(error -> {
                    var errorMessage = error.getDefaultMessage();
                    errors.add(errorMessage);
              });
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ExceptionResponse
                               .builder()
                                .validationErrors(errors)
                               .build()
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exp) {
        log.info("");
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(
                        ExceptionResponse
                               .builder()
                                .businessErrorDescription("Internal error, contact the administrator")
                                .error(exp.getMessage())
                               .build()
                );
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<ExceptionResponse> handleException(OperationNotPermittedException exp) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(
                        ExceptionResponse
                               .builder()
                                .error(exp.getMessage())
                               .build()
                );
    }
}
