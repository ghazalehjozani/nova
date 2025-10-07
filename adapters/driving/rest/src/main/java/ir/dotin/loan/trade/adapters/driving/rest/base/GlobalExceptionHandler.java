package ir.dotin.loan.trade.adapters.driving.rest.base;

import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public ResponseEntity<ServiceResponse<?>> handle(Exception exception) {
        ServiceError serviceError = ServiceError.builder()
                .code("500")
                .message(exception.getMessage())
                .details(Collections.singletonList(ServiceError.ErrorDetail.of(exception.toString())))
                .build();
        ServiceResponse<Object> error = ServiceResponse.error(serviceError);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
