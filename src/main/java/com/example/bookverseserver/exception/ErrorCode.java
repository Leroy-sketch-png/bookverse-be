package com.example.bookverseserver.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT(1001, "Invalid input data", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1002, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1003, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_REQUEST(1004, "Invalid request", HttpStatus.BAD_REQUEST ),

    // USER
    USER_EXISTED(1100, "User already exists", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1101,"User not exists", HttpStatus.BAD_REQUEST ),
    USER_NOT_FOUND(1102, "User not found", HttpStatus.NOT_FOUND),

    // BOOK
    BOOK_EXISTED(1200, "Book already exists", HttpStatus.BAD_REQUEST),
    BOOK_NOT_FOUND(1201, "Book not found", HttpStatus.NOT_FOUND),
    BOOK_IMAGE_UPLOAD_FAILED(1202, "Book cover upload failed", HttpStatus.BAD_REQUEST),

    // CATEGORY
    CATEGORY_NOT_FOUND(1300, "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_EXISTED(1301, "Category already exists", HttpStatus.BAD_REQUEST),

    // INVENTORY

    // REVIEW
    REVIEW_NOT_FOUND(1500, "Review not found", HttpStatus.NOT_FOUND),

    // GENERAL FILE/UPLOAD
    FILE_UPLOAD_FAILED(1600, "File upload failed", HttpStatus.BAD_REQUEST),
    FILE_DELETE_FAILED(1601, "File delete failed", HttpStatus.BAD_REQUEST),

    INVALID_KEY(1700, "Invalid key" , HttpStatus.BAD_REQUEST ),
    ROLE_NOT_FOUND(1701, "Role not found" , HttpStatus.NOT_FOUND ),
    NO_AUTHOR_FOUND(1702,"No author found" , HttpStatus.BAD_REQUEST),
    AUTHOR_EXISTED(1703,"Author already exists" , HttpStatus.BAD_REQUEST ),
    EMAIL_NOT_EXISTED(1704,"Email not existed" , HttpStatus.NOT_FOUND ),
    USERNAME_NOT_EXISTED(1705,"Username not existed" , HttpStatus.NOT_FOUND ),
    PROFILE_ALREADY_EXISTS(1706, "Profile already exists" , HttpStatus.BAD_REQUEST ),
    PROFILE_NOT_FOUND(1707,"Profile not found" , HttpStatus.NOT_FOUND ),
    AUTHOR_NOT_EXISTED(1708,"Author not existed" , HttpStatus.NOT_FOUND ),
    DO_NOT_HAVE_PERMISSION(1709, "You do not have this permission!" , HttpStatus.FORBIDDEN ),
    LISTING_NOT_EXISTED(1710, "Listing not existed" , HttpStatus.NOT_FOUND ),
    TERMS_NOT_ACCEPTED(1711, "Terms not accepted", HttpStatus.BAD_REQUEST),
    NO_LISTING_FOUND(1712, "No listing found" , HttpStatus.BAD_REQUEST ),;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
