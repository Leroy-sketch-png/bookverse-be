package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.Cart.CartResponse;
import com.example.bookverseserver.dto.response.CartItem.CartItemForCartResponse;
import com.example.bookverseserver.dto.response.Voucher.VoucherCartResponse;
import com.example.bookverseserver.entity.Order_Payment.Cart;
import com.example.bookverseserver.entity.Order_Payment.Voucher;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.CartItemMapper;
import com.example.bookverseserver.repository.CartItemRepository;
import com.example.bookverseserver.repository.CartRepository;
import com.example.bookverseserver.repository.UserRepository;
import com.example.bookverseserver.repository.VoucherRepository;
import com.example.bookverseserver.service.discount.DiscountStrategy;
import com.example.bookverseserver.service.discount.DiscountStrategyFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {

    CartRepository cartRepository;
    UserRepository userRepository;
    DiscountStrategyFactory discountStrategyFactory;
    CartItemRepository cartItemRepository;
    CartItemMapper cartItemMapper;
    VoucherRepository voucherRepository;

    private VoucherCartResponse getVoucher(Cart savedCart) {
        if (savedCart.getVoucher() == null) {
            return null;
        }
        return VoucherCartResponse.builder()
                .id(savedCart.getVoucher().getId())
                .code(savedCart.getVoucher().getCode())
                .discountType(savedCart.getVoucher().getDiscountType())
                .discountValue(savedCart.getVoucher().getDiscountValue())
                .minOrderValue(savedCart.getVoucher().getMinOrderValue())
                .maxUsagePerUser(savedCart.getVoucher().getMaxUsagePerUser())
                .build();
    }

    public BigDecimal getDiscount(Cart savedCart) {
        if (savedCart.getVoucher() == null) {
            return BigDecimal.ZERO;
        }
        DiscountStrategy discountStrategy = discountStrategyFactory.getStrategy(savedCart.getVoucher().getDiscountType());
        return discountStrategy.calculateDiscount(savedCart.getTotalPrice(), savedCart.getVoucher().getDiscountValue(), savedCart.getVoucher().getMinOrderValue());
    }

    public CartResponse getCartByUserId(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        var cart = cartRepository.findByUserId(userId);
        if (cart.isEmpty()) {
            Cart newCart = Cart.builder()
                    .user(user)
                    .build();

            Cart savedCart = cartRepository.save(newCart);
            return CartResponse.builder()
                    .id(savedCart.getId())
                    .userId(savedCart.getUser().getId())
                    .totalPrice(savedCart.getTotalPrice())
                    .discount(getDiscount(savedCart))
                    .voucher(getVoucher(savedCart))
                    .cartItems(getCartItems(savedCart))
                    .build();
        } else {
            Cart existingCart = cart.get();
            return CartResponse.builder()
                    .id(existingCart.getId())
                    .userId(existingCart.getUser().getId())
                    .totalPrice(existingCart.getTotalPrice())
                    .discount(getDiscount(existingCart))
                    .voucher(getVoucher(existingCart))
                    .cartItems(getCartItems(existingCart))
                    .build();
        }

    }

    private Set<CartItemForCartResponse> getCartItems(Cart savedCart) {
        if (savedCart.getCartItems() == null) {
            return Set.of();
        }
        return savedCart.getCartItems().stream()
                .map( cartItem -> cartItemMapper.toCartItemForCartResponse(cartItem))
                .collect(Collectors.toSet());
    }


    public CartResponse applyVoucherToCart(Long userId, String voucherCode) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        Voucher voucher = voucherRepository.findByCode(voucherCode)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        // Kiểm tra voucher còn active không
        if (!voucher.getIsActive()) {
            throw new AppException(ErrorCode.VOUCHER_NOT_FOUND);
        }

        // Kiểm tra voucher còn hạn không
        if (voucher.getValidTo() != null && voucher.getValidTo().isBefore(java.time.LocalDateTime.now())) {
            throw new AppException(ErrorCode.VOUCHER_NOT_FOUND);
        }

        // Kiểm tra minOrderValue
        if (voucher.getMinOrderValue() != null && cart.getTotalPrice().compareTo(voucher.getMinOrderValue()) < 0) {
            throw new AppException(ErrorCode.VOUCHER_MIN_ORDER_VALUE_NOT_MET);
        }

        cart.setVoucher(voucher);
        Cart savedCart = cartRepository.save(cart);

        return CartResponse.builder()
                .id(savedCart.getId())
                .userId(savedCart.getUser().getId())
                .totalPrice(savedCart.getTotalPrice())
                .discount(getDiscount(savedCart))
                .voucher(getVoucher(savedCart))
                .cartItems(getCartItems(savedCart))
                .build();
    }

    public CartResponse removeVoucherFromCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        cart.setVoucher(null);
        Cart savedCart = cartRepository.save(cart);

        return CartResponse.builder()
                .id(savedCart.getId())
                .userId(savedCart.getUser().getId())
                .totalPrice(savedCart.getTotalPrice())
                .discount(getDiscount(savedCart))
                .voucher(getVoucher(savedCart))
                .cartItems(getCartItems(savedCart))
                .build();
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        // 1. Xóa items trong DB
        cartItemRepository.deleteAllByCartId(cart.getId());

        // 2. Reset cart state
        cart.getCartItems().clear(); // Clear list trong memory
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setVoucher(null); // Clear voucher khi clear cart

        // 3. Save cart rỗng
        cartRepository.save(cart);
    }
}
