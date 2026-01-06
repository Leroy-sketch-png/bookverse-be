package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Book.AuthorDetailRequest;
import com.example.bookverseserver.dto.request.Book.AuthorRequest;
import com.example.bookverseserver.dto.response.Book.AuthorDetailResponse;
import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import com.example.bookverseserver.entity.Product.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface AuthorMapper {
    @Mapping(target = "bookMetas", ignore = true)
    Author toAuthor(AuthorRequest author);

    @Mapping(source = "birthDate", target = "dob")
    @Mapping(source = "openLibraryId", target = "openLibraryId")
    AuthorResponse toAuthorResponse(Author author);

    @Mapping(source = "bio", target = "biography")
    @Mapping(source = "avatar", target = "avatarUrl")
    AuthorDetailResponse toAuthorDetailResponse(Author author);

    @Mapping(target = "bookMetas", ignore = true)
    void updateAuthor(@MappingTarget Author author, AuthorDetailRequest request);
}
