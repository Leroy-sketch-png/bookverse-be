package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Book.BookRequest;
import com.example.bookverseserver.dto.response.Book.BookResponse;
import com.example.bookverseserver.entity.Product.Book;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {
    @Mapping(target = "publishedDate", source = "publishedDate")
    @Mapping(target = "price", source = "price")
    Book toBook(BookRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)        // never overwrite ID
    @Mapping(target = "seller", ignore = true)    // seller should not be updated here
    void updateBook(@MappingTarget Book book, BookRequest request);

    @Mapping(source = "seller.id", target = "sellerId")
    @Mapping(source = "seller.username", target = "sellerName")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "inventory.stockQuantity", target = "stockQuantity")
    BookResponse toBookResponse(Book book);

    // Convert List of Book entities to List of responses
    List<BookResponse> toBookResponseList(List<Book> books);
}
