package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Book.BookMetaCreationRequest;
import com.example.bookverseserver.dto.response.Book.BookResponse;
import com.example.bookverseserver.entity.Product.BookMeta;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMetaMapper {
    BookMeta toBookMeta(BookMetaCreationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBookMeta(@MappingTarget BookMeta bookMeta, BookMetaCreationRequest request);

    BookResponse toBookResponse(BookMeta bookMeta);

    List<BookResponse> toBookResponseList(List<BookMeta> bookMetas);
}
