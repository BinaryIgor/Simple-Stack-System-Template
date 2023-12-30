package com.binaryigor.main._commons.app;

import com.binaryigor.main._commons.exception.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionsHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionsHandler.class);

    //TODO: sth better
    @ExceptionHandler
    ResponseEntity<String> handleAppException(AppException exception) {
        log.warn("Unhandled error:", exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(String.join(", ", exception.toErrors()));
    }
}
