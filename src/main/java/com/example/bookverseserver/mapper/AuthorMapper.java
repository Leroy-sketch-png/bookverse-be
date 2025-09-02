package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Book.AuthorRequest;
import com.example.bookverseserver.dto.request.User.UserUpdateRequest;
import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import com.example.bookverseserver.entity.Product.Author;
import com.example.bookverseserver.entity.User.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AuthorMapper {
    @Mapping(target = "books", ignore = true)
    Author toAuthor(AuthorRequest author);

    AuthorResponse toAuthorResponse(Author author);

    @Mapping(target = "books", ignore = true)
    void updateAuthor(@MappingTarget Author author, AuthorRequest request);
}
