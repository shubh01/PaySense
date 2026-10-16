package com.inn.paysense.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class PaySenseGlobalExceptionHandler {

    @ExceptionHandler(PaySenseException.class)
    public ResponseEntity<ExceptionResponse> handlePaySenseException(PaySenseException e){
        ExceptionResponse exceptionResponse = new ExceptionResponse(e.getStatus(), e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatusCode.valueOf(e.getStatus()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception e){
        ExceptionResponse exceptionResponse = new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorMessageConstants.GENERIC_EXCEPTION_MESSAGE + e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }


}
