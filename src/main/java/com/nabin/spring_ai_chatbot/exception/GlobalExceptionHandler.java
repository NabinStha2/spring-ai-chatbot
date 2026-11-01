package com.nabin.spring_ai_chatbot.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiErrorResponse> handleException(ApplicationException ex,
                                                            HttpServletRequest req) {
        return buildResponse(ex.getErrorCode(), ex.getMessage(), ex.getHttpStatus(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest req) {
        List<ApiErrorResponse.FieldError> fieldErrors = ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f -> new ApiErrorResponse.FieldError(
                        f.getField(),
                        f.getDefaultMessage() != null ? f.getDefaultMessage() : "Invalid value"
                ))
                .toList();

        return buildResponse("VALIDATION_FAILED", "Request validation failed",
                             HttpStatus.BAD_REQUEST, fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(
            Exception ex,
            HttpServletRequest req
                                               ) {
        return buildResponse(
                "INTERNAL_ERROR",
                "Something went wrong",
                HttpStatus.INTERNAL_SERVER_ERROR,
                List.of()
                            );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(String errorCode, String message,
                                                           HttpStatus status,
                                                           List<ApiErrorResponse.FieldError> fieldErrors) {
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(errorCode, message,
                                                                 status.value(), fieldErrors);

        return ResponseEntity
                .status(status)
                .body(apiErrorResponse);
    }
}
