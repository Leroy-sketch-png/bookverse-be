package com.example.bookverseserver.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // --- 500 INTERNAL SERVER ERROR (Lỗi hệ thống) ---
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_FAILED(500, "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAILED(500, "File delete failed", HttpStatus.INTERNAL_SERVER_ERROR),
    CANNOT_SEND_EMAIL(500, "Cannot send email", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_INTENT_CREATION_FAILED(500, "Failed to create payment intent", HttpStatus.INTERNAL_SERVER_ERROR),

    // --- 400 BAD REQUEST (Lỗi dữ liệu đầu vào / Logic) ---
    INVALID_KEY(400, "Invalid enum key", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(400, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_INPUT(400, "Invalid input data", HttpStatus.BAD_REQUEST),
    INVALID_OTP(400, "Invalid OTP", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(400, "OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_NOT_FOUND(400, "Invalid changing password request", HttpStatus.BAD_REQUEST),
    PASSWORDS_MISMATCH(400, "Passwords do not match", HttpStatus.BAD_REQUEST),
    TERMS_NOT_ACCEPTED(400, "Terms not accepted", HttpStatus.BAD_REQUEST),

    // Logic nghiệp vụ (Cart, Order, Voucher)
    CART_EMPTY(400, "Cart is empty", HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY(400, "Invalid quantity", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK(400, "Insufficient stock", HttpStatus.BAD_REQUEST), // Đã sửa dòng này
    NOT_ENOUGH_LISTING(400, "Not enough listing quantity", HttpStatus.BAD_REQUEST),

    VOUCHER_CODE_REQUIRED(400, "Voucher code is required.", HttpStatus.BAD_REQUEST),
    VOUCHER_DISCOUNT_TYPE_REQUIRED(400, "Discount type is required.", HttpStatus.BAD_REQUEST),
    VOUCHER_DISCOUNT_TYPE_INVALID(400, "Invalid discount type.", HttpStatus.BAD_REQUEST),
    VOUCHER_DISCOUNT_VALUE_REQUIRED(400, "Discount value is required.", HttpStatus.BAD_REQUEST),
    VOUCHER_DISCOUNT_VALUE_INVALID(400, "Discount value must be positive.", HttpStatus.BAD_REQUEST),
    VOUCHER_VALID_TO_REQUIRED(400, "Valid to date is required.", HttpStatus.BAD_REQUEST),
    VOUCHER_MIN_ORDER_VALUE_INVALID(400, "Minimum order value must be positive.", HttpStatus.BAD_REQUEST),
    VOUCHER_MAX_USAGE_PER_USER_REQUIRED(400, "Max usage per user is required.", HttpStatus.BAD_REQUEST),
    VOUCHER_MAX_USAGE_PER_USER_INVALID(400, "Max usage per user must be positive.", HttpStatus.BAD_REQUEST),
    VOUCHER_MIN_ORDER_VALUE_NOT_MET(400, "Order value does not meet the minimum requirement.", HttpStatus.BAD_REQUEST),

    CANNOT_CANCEL_ORDER(400, "Cannot cancel this order (already processed or shipped)", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED(400, "Payment failed", HttpStatus.BAD_REQUEST),

    // Address Validation
    SHIPPING_ADDRESS_FULL_NAME_REQUIRED(400, "Full name is required", HttpStatus.BAD_REQUEST),
    SHIPPING_ADDRESS_FULL_NAME_TOO_LONG(400, "Full name too long", HttpStatus.BAD_REQUEST),
    SHIPPING_ADDRESS_PHONE_REQUIRED(400, "Phone is required", HttpStatus.BAD_REQUEST),
    SHIPPING_ADDRESS_PHONE_INVALID(400, "Invalid phone number", HttpStatus.BAD_REQUEST),
    SHIPPING_ADDRESS_LINE1_REQUIRED(400, "Address line 1 is required", HttpStatus.BAD_REQUEST),
    SHIPPING_ADDRESS_CITY_REQUIRED(400, "City is required", HttpStatus.BAD_REQUEST),
    SHIPPING_ADDRESS_CITY_TOO_LONG(400, "City name too long", HttpStatus.BAD_REQUEST),
    SHIPPING_ADDRESS_POSTAL_CODE_TOO_LONG(400, "Postal code too long", HttpStatus.BAD_REQUEST),
    SHIPPING_ADDRESS_COUNTRY_REQUIRED(400, "Country is required", HttpStatus.BAD_REQUEST),
    SHIPPING_ADDRESS_COUNTRY_TOO_LONG(400, "Country name too long", HttpStatus.BAD_REQUEST),

    // --- 401 UNAUTHORIZED (Chưa đăng nhập / Token sai) ---
    UNAUTHENTICATED(401, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    INVALID_SUBJECT_IN_JWT(401, "Invalid subject in JWT", HttpStatus.UNAUTHORIZED),

    // --- 403 FORBIDDEN (Không có quyền) ---
    UNAUTHORIZED(403, "You do not have permission", HttpStatus.FORBIDDEN),
    DO_NOT_HAVE_PERMISSION(403, "You do not have this permission!", HttpStatus.FORBIDDEN),

    // --- 404 NOT FOUND (Không tìm thấy) ---
    USER_NOT_FOUND(404, "User not found", HttpStatus.NOT_FOUND),
    USER_NOT_EXISTED(404, "User does not exist", HttpStatus.NOT_FOUND), // Có thể merge với USER_NOT_FOUND
    EMAIL_NOT_EXISTED(404, "Email not existed", HttpStatus.NOT_FOUND),
    USERNAME_NOT_EXISTED(404, "Username not existed", HttpStatus.NOT_FOUND),
    PROFILE_NOT_FOUND(404, "Profile not found", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND(404, "Role not found", HttpStatus.NOT_FOUND),

    BOOK_NOT_FOUND(404, "Book not found", HttpStatus.NOT_FOUND),
    BOOK_META_NOT_FOUND(404, "Book meta not found", HttpStatus.NOT_FOUND),
    BOOK_NOT_FOUND_IN_OPEN_LIBRARY(404, "Book not found in open library", HttpStatus.NOT_FOUND),

    CATEGORY_NOT_FOUND(404, "Category not found", HttpStatus.NOT_FOUND),
    AUTHOR_NOT_FOUND(404, "Author not found", HttpStatus.NOT_FOUND),
    AUTHOR_NOT_EXISTED(404, "Author not existed", HttpStatus.NOT_FOUND),
    NO_AUTHOR_FOUND(404, "No author found", HttpStatus.BAD_REQUEST), // Lưu ý: logic cũ để BAD_REQUEST, cân nhắc đổi sang NOT_FOUND

    LISTING_NOT_FOUND(404, "Listing not found", HttpStatus.NOT_FOUND),
    LISTING_NOT_EXISTED(404, "Listing not existed", HttpStatus.NOT_FOUND),
    NO_LISTING_FOUND(404, "No listing found", HttpStatus.NOT_FOUND),

    REVIEW_NOT_FOUND(404, "Review not found", HttpStatus.NOT_FOUND),

    CART_NOT_FOUND(404, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(404, "Cart item not found", HttpStatus.NOT_FOUND),

    ORDER_NOT_FOUND(404, "Order not found", HttpStatus.NOT_FOUND),
    SHIPPING_ADDRESS_NOT_FOUND(404, "Shipping address not found", HttpStatus.NOT_FOUND),
    VOUCHER_NOT_FOUND(404, "Voucher not found", HttpStatus.NOT_FOUND),

    // --- 409 CONFLICT (Trùng lặp dữ liệu) ---
    USER_EXISTED(409, "User already exists", HttpStatus.CONFLICT),
    PROFILE_ALREADY_EXISTS(409, "Profile already exists", HttpStatus.CONFLICT),

    BOOK_EXISTED(409, "Book already exists", HttpStatus.CONFLICT),
    CATEGORY_EXISTED(409, "Category already exists", HttpStatus.CONFLICT),
    AUTHOR_EXISTED(409, "Author already exists", HttpStatus.CONFLICT),
    LISTING_EXISTED(409, "Listing already exists", HttpStatus.CONFLICT),

    VOUCHER_ALREADY_EXISTS(409, "Voucher code already exists", HttpStatus.CONFLICT),
    VOUCHER_EXPIRED(409, "Voucher code expired", HttpStatus.CONFLICT ),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}