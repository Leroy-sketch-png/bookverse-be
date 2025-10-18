package com.example.bookverseserver.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT(1001, "Invalid input data", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1002, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    INVALID_SUBJECT_IN_JWT(1005, "Invalid subject in JWT", HttpStatus.UNAUTHORIZED),
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

    // AUTHOR
    AUTHOR_NOT_FOUND(1400, "Author not found", HttpStatus.NOT_FOUND),
    AUTHOR_EXISTED(1401, "Author already exists", HttpStatus.BAD_REQUEST),

    // LISTING
    LISTING_NOT_FOUND(1500, "Listing not found", HttpStatus.NOT_FOUND),
    LISTING_EXISTED(1501, "Listing already exists", HttpStatus.BAD_REQUEST),

    // REVIEW
    REVIEW_NOT_FOUND(1600, "Review not found", HttpStatus.NOT_FOUND),

    // GENERAL FILE/UPLOAD
    FILE_UPLOAD_FAILED(1700, "File upload failed", HttpStatus.BAD_REQUEST),
    FILE_DELETE_FAILED(1701, "File delete failed", HttpStatus.BAD_REQUEST),

    INVALID_KEY(1720, "Invalid key" , HttpStatus.BAD_REQUEST ),
    ROLE_NOT_FOUND(1721, "Role not found" , HttpStatus.NOT_FOUND ),
    NO_AUTHOR_FOUND(1702,"No author found" , HttpStatus.BAD_REQUEST),
    EMAIL_NOT_EXISTED(1704,"Email not existed" , HttpStatus.NOT_FOUND ),
    USERNAME_NOT_EXISTED(1705,"Username not existed" , HttpStatus.NOT_FOUND ),
    PROFILE_ALREADY_EXISTS(1706, "Profile already exists" , HttpStatus.BAD_REQUEST ),
    PROFILE_NOT_FOUND(1707,"Profile not found" , HttpStatus.NOT_FOUND ),
    AUTHOR_NOT_EXISTED(1708,"Author not existed" , HttpStatus.NOT_FOUND ),
    DO_NOT_HAVE_PERMISSION(1709, "You do not have this permission!" , HttpStatus.FORBIDDEN ),
    LISTING_NOT_EXISTED(1710, "Listing not existed" , HttpStatus.NOT_FOUND ),
    TERMS_NOT_ACCEPTED(1711, "Terms not accepted", HttpStatus.BAD_REQUEST),
    NO_LISTING_FOUND(1712, "No listing found" , HttpStatus.BAD_REQUEST ),
    BOOK_META_NOT_FOUND(1713, "Book meta not found" , HttpStatus.NOT_FOUND ),
    NOT_ENOUGH_LISTING(1714, "Not enough listing" , HttpStatus.BAD_REQUEST ),
    BOOK_NOT_FOUND_IN_OPEN_LIBRARY(1715, "Book not found in open library" , HttpStatus.NOT_FOUND ),
    PASSWORDS_MISMATCH(1716,"Passwords mismatch" ,HttpStatus.BAD_REQUEST ),
    OTP_NOT_FOUND(1717,"Invalid changing password request" ,HttpStatus.BAD_REQUEST ),
    INVALID_OTP(1718,"Invalid OTP" , HttpStatus.BAD_REQUEST ),
    OTP_EXPIRED(1719,"OTP expired" , HttpStatus.BAD_REQUEST ),
    CANNOT_SEND_EMAIL(1720,"Cannot send email" ,HttpStatus.BAD_REQUEST );


    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
