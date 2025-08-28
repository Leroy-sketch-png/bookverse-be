package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Book.BookRequest;
import com.example.bookverseserver.dto.response.Book.BookResponse;
import com.example.bookverseserver.entity.Product.BookMeta;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {

    // Map BookRequest to Book entity
//    @Mapping(target = "author", ignore = true) // sẽ set author trong service
//    @Mapping(target = "category", ignore = true) // set category trong service
    BookMeta toBook(BookRequest request);

    // Update existing Book entity from BookRequest
//    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "seller", ignore = true)
//    @Mapping(target = "author", ignore = true)
//    @Mapping(target = "category", ignore = true)
    void updateBook(@MappingTarget BookMeta bookMeta, BookRequest request);

    // Map Book entity to BookResponse DTO
//    @Mapping(source = "author.id", target = "authorId")
//    @Mapping(source = "author.name", target = "authorName")
//    @Mapping(source = "category.id", target = "categoryId")
//    @Mapping(source = "category.name", target = "categoryName")
//    @Mapping(source = "seller.id", target = "sellerId")
//    @Mapping(source = "seller.username", target = "sellerName")
    BookResponse toBookResponse(BookMeta bookMeta);

    // Map a list of Book entities to a list of BookResponse DTOs
    List<BookResponse> toBookResponseList(List<BookMeta> bookMetas);
}
