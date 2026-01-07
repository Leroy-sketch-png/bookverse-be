package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.CartItem.CartItemRequest;
import com.example.bookverseserver.dto.response.Cart.CartItemBookResponse;
import com.example.bookverseserver.dto.response.Cart.CartItemListingResponse;
import com.example.bookverseserver.dto.response.Cart.CartItemSellerResponse;
import com.example.bookverseserver.dto.response.CartItem.CartItemForCartResponse;
import com.example.bookverseserver.dto.response.CartItem.CartItemResponse;
import com.example.bookverseserver.entity.Order_Payment.CartItem;
import com.example.bookverseserver.entity.Product.Listing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    CartItemRequest toResponse(CartItem cartItem);

    CartItemResponse toCartItemResponse(CartItem cartItem);

    /**
     * Maps CartItem to CartItemForCartResponse with full nested listing structure.
     * Matches Vision API_CONTRACTS.md §Cart specification.
     */
    @Mapping(target = "listing", source = "listing", qualifiedByName = "toCartItemListingResponse")
    @Mapping(target = "quantity", source = "quantity")
    CartItemForCartResponse toCartItemForCartResponse(CartItem cartItem);

    @Named("toCartItemListingResponse")
    default CartItemListingResponse toCartItemListingResponse(Listing listing) {
        if (listing == null) return null;
        
        // Build book info
        CartItemBookResponse book = CartItemBookResponse.builder()
                .title(listing.getTitleOverride() != null 
                        ? listing.getTitleOverride() 
                        : (listing.getBookMeta() != null ? listing.getBookMeta().getTitle() : "Unknown"))
                .coverImage(listing.getBookMeta() != null ? listing.getBookMeta().getCoverImageUrl() : null)
                .build();
        
        // Build seller info
        CartItemSellerResponse seller = CartItemSellerResponse.builder()
                .id(listing.getSeller() != null ? listing.getSeller().getId() : null)
                .name(listing.getSeller() != null && listing.getSeller().getUserProfile() != null 
                        ? listing.getSeller().getUserProfile().getDisplayName() 
                        : (listing.getSeller() != null ? listing.getSeller().getUsername() : "Unknown"))
                .build();
        
        // Build listing response
        return CartItemListingResponse.builder()
                .id(listing.getId())
                .book(book)
                .price(listing.getPrice())
                .finalPrice(listing.getPrice())  // Same as price for now (no promotions)
                .quantity(listing.getQuantity())  // Available stock
                .condition(listing.getCondition())
                .seller(seller)
                .build();
    }
}

