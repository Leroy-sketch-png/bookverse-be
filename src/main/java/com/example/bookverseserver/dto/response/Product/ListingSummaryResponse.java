package com.example.bookverseserver.dto.response.Product;

import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.enums.BookCondition;
import com.example.bookverseserver.enums.ListingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListingSummaryResponse {

    Long id;

    // Thông tin hiển thị chính
    String displayTitle; // Sẽ ưu tiên titleOverride, nếu null thì lấy bookTitle
    BigDecimal price;
    String currency;

    // Hình ảnh đại diện (chỉ cần 1 ảnh đầu tiên thay vì List)
    String mainPhotoUrl;

    // Tình trạng & Người bán
    BookCondition condition;
    String sellerName;
    Long sellerId; // Cần thiết để navigate vào trang profile shop

    // Category information
    CategoryResponse category;

    // Trạng thái tồn kho & hiển thị
    ListingStatus status;
    boolean inStock; // check từ quantity > 0

    // Social proof (tùy chọn, tốt cho hiển thị card)
    Integer likes;
    Integer soldCount;
}