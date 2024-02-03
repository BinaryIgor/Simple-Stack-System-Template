package com.binaryigor.guardian.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionsHandler {

    private static final Logger log = LoggerFactory.getLogger(ExceptionsHandler.class);

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ExceptionResponse> handleException(Throwable exception) {
        log.error("Unhandled exception...", exception);
        return ResponseEntity.badRequest().body(new ExceptionResponse(exception));
    }
}
