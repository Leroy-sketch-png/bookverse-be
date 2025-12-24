package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.CartItem.CartItemRequest;
import com.example.bookverseserver.dto.response.CartItem.CartItemResponse;
import com.example.bookverseserver.entity.Order_Payment.Cart;
import com.example.bookverseserver.entity.Order_Payment.CartItem;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.CartItemRepository;
import com.example.bookverseserver.repository.CartRepository;
import com.example.bookverseserver.repository.ListingRepository;
import com.example.bookverseserver.repository.UserRepository;
import com.example.bookverseserver.service.discount.DiscountStrategy;
import com.example.bookverseserver.service.discount.DiscountStrategyFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class CartItemService {

    CartItemRepository cartItemRepository;
    CartRepository cartRepository;
    UserRepository userRepository;
    ListingRepository listingRepository;
    DiscountStrategyFactory discountStrategyFactory;

    private BigDecimal calculateDiscount(Cart cart) {
        if (cart.getVoucher() == null) {
            return BigDecimal.ZERO;
        }
        DiscountStrategy discountStrategy = discountStrategyFactory.getStrategy(cart.getVoucher().getDiscountType());
        return discountStrategy.calculateDiscount(
                cart.getTotalPrice(),
                cart.getVoucher().getDiscountValue(),
                cart.getVoucher().getMinOrderValue()
        );
    }

    @Transactional
    public CartItemResponse createCartItem(Long userId, CartItemRequest cartItemRequest) {
        // 1. Lấy user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Lấy listing
        Listing listing = listingRepository.findById(cartItemRequest.listingId())
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));

        int quantityToAdd = cartItemRequest.quantity();
        int currentStock = listing.getQuantity();

        if (quantityToAdd > currentStock) {
            throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
        }

        // 3. Lấy hoặc tạo cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .totalPrice(BigDecimal.ZERO)
                            .build();
                    return cartRepository.save(newCart);
                });

        // 4. Lấy hoặc tạo cart item
        CartItem cartItem = cartItemRepository.findByCartIdAndListingId(cart.getId(), cartItemRequest.listingId())
                .map(existingItem -> {
                    existingItem.setQuantity(existingItem.getQuantity() + cartItemRequest.quantity());
                    return existingItem;
                })
                .orElseGet(() -> CartItem.builder()
                        .listing(listing)
                        .quantity(cartItemRequest.quantity())
                        .cart(cart)
                        .build());


        cartItemRepository.save(cartItem);


        BigDecimal addedPrice = listing.getPrice().multiply(BigDecimal.valueOf(cartItemRequest.quantity()));
        BigDecimal currentTotal = cart.getTotalPrice() != null ? cart.getTotalPrice() : BigDecimal.ZERO;
        cart.setTotalPrice(currentTotal.add(addedPrice));

        Cart savedCart = cartRepository.save(cart);


        return CartItemResponse.builder()
                .totalPrice(savedCart.getTotalPrice())
                .discount(calculateDiscount(savedCart))
                .build();
    }

    @Transactional
    public CartItemResponse updateCartItem(Long userId, Long listingId, Integer quantity) {

        if(quantity <= 0) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }

        CartItem cartItem = cartItemRepository.findByCartUserIdAndListingId(userId, listingId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        // Cập nhật tổng giá của giỏ hàng
        BigDecimal currentTotal = cartItem.getCart().getTotalPrice() != null
                ? cartItem.getCart().getTotalPrice() : BigDecimal.ZERO;
        BigDecimal itemPrice = cartItem.getListing().getPrice();
        BigDecimal oldSubtotal = itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        BigDecimal newSubtotal = itemPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal newTotal = currentTotal.subtract(oldSubtotal).add(newSubtotal);
        cartItem.getCart().setTotalPrice(newTotal);

        cartItem.setQuantity(quantity);
        CartItem savedCartItem = cartItemRepository.save(cartItem);


        return CartItemResponse.builder()
                .totalPrice(savedCartItem.getCart().getTotalPrice())
                .discount(calculateDiscount(savedCartItem.getCart()))
                .build();
    }

    public CartItemResponse deleteCartItem(Long userId, Long listingId) {
        CartItem cartItem = cartItemRepository.findByCartUserIdAndListingId(userId, listingId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        // Cập nhật tổng giá của giỏ hàng
        BigDecimal currentTotal = cartItem.getCart().getTotalPrice() != null
                ? cartItem.getCart().getTotalPrice() : BigDecimal.ZERO;
        BigDecimal itemPrice = cartItem.getListing().getPrice();
        BigDecimal subtotal = itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        BigDecimal newTotal = currentTotal.subtract(subtotal);
        cartItem.getCart().setTotalPrice(newTotal);

        cartItemRepository.delete(cartItem);

        return CartItemResponse.builder()
                .totalPrice(cartItem.getCart().getTotalPrice())
                .discount(calculateDiscount(cartItem.getCart()))
                .build();
    }
}
