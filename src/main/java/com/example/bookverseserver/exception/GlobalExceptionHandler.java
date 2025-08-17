package com.example.bookverseserver.exception;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import com.example.bookverseserver.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String MIN_ATTRIBUTE = "min";

//    @ExceptionHandler(value = RuntimeException.class)
//    ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException ex, WebRequest request){
//        ApiResponse response = new ApiResponse();
//
//        response.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
//        response.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
//        return ResponseEntity.badRequest().body(response);
//    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException ex){
        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse response = new ApiResponse();

        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMessage());
        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(response);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex){
        String enumKey = ex.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;
        try {
            errorCode = ErrorCode.valueOf(enumKey);
            var constraintViolation = ex.getBindingResult()
                    .getAllErrors().getFirst().unwrap(ConstraintViolation.class);

            attributes = constraintViolation.getConstraintDescriptor().getAttributes();
            log.info(attributes.toString());
        }
        catch (IllegalArgumentException e) {

        }
        ApiResponse response = new ApiResponse();
        response.setCode(errorCode.getCode());
        response.setMessage(Objects.nonNull(attributes) ?
                mapAttributes(errorCode.getMessage(), attributes) : errorCode.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException ex){
        ErrorCode errorCode =  ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    private String mapAttributes(String message, Map<String, Object> attributes){
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }

}