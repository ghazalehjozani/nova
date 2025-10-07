package ir.dotin.loan.trade.adapters.driving.rest.base;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public ResponseEntity<ServiceError> handle(Exception exception) {
        ServiceError details = ServiceError.builder()
                .code("500")
                .message(exception.getMessage())
                .details(Collections.singletonList(ServiceError.ErrorDetail.of(exception.toString())))
                .build();

        return new ResponseEntity<>(details, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
