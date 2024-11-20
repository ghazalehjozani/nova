package ir.dotin.loan.morabehe.adapters.web.advice;

import ir.dotin.platform.ddd.common.exception.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<String> handle(DomainException e) {
        return ResponseEntity.badRequest().body(e.getLocalizedMessage());
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handle(RuntimeException e) {
        return ResponseEntity.internalServerError()
                .body(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage());
    }

}

