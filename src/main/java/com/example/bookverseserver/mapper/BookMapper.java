//package com.example.bookverseserver.mapper;
//
//import com.example.bookverseserver.dto.request.Book.BookRequest;
//import com.example.bookverseserver.dto.response.Book.BookResponse;
//import com.example.bookverseserver.entity.Product.Book;
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import org.mapstruct.MappingTarget;
//
//import java.util.List;
//
//@Mapper(componentModel = "spring")
//public interface BookMapper {
//
//    // Convert BookRequest to Book Entity (for create)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "isDeleted", ignore = true)
//    Book toBook(BookRequest request);
//
//    // Update existing Book entity using BookRequest (for update)
//    @Mapping(target = "bookId", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    @Mapping(target = "isDeleted", ignore = true)
//    void updateBook(@MappingTarget Book book, BookRequest request);
//
//    // Convert Book entity to BookResponse (for returning to client)
//    @Mapping(source = "seller.id", target = "sellerId")
//    @Mapping(source = "seller.username", target = "sellerUsername")
//    @Mapping(source = "category.categoryId", target = "categoryId")
//    @Mapping(source = "category.categoryName", target = "categoryName")
//    @Mapping(source = "isDeleted", target = "deleted")
//    BookResponse toBookResponse(Book book);
//
//    // Convert List of Book entities to List of responses
//    List<BookResponse> toBookResponseList(List<Book> books);
//}
