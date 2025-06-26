package com.example.allrealmen.common.exception;

import com.example.allrealmen.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.HttpStatus;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({SQLException.class})
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({NullPointerException.class})
    public ResponseEntity<ApiResponse<Object>> handleNullPointerException(NullPointerException e) {
        ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ApiResponse<Object>> hanadleAuthenticationFailed(AuthenticationException e){
        ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
        log.info(e.getMessage(), e);
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({HttpClientErrorException.class})
    public ResponseEntity<ApiResponse<Object>> handleHttpClientErrorException(HttpClientErrorException e){
        ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
        return new ResponseEntity<>(apiResponse, e.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handlerOtherException(Exception e) {
        log.error(e.getMessage(), e);
        ApiResponse<Object> apiResponse = ApiResponse.error(e.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.valueOf(400));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Object> apiResponse = ApiResponse.error(ex.toString());
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        ApiResponse<Object> apiResponse = ApiResponse.error(ex.getMessage());
        return new ResponseEntity<>(apiResponse, statusCode);
    }
} 