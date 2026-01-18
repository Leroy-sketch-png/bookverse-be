package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.response.Cart.CartResponse;
import com.example.bookverseserver.dto.response.Cart.CartSummary;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)  // Default to read-only, override for write methods
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

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .totalPrice(BigDecimal.ZERO)
                            .build();
                    return cartRepository.save(newCart);
                });

        return buildCartResponse(cart);
    }

    /**
     * Builds CartResponse matching Vision API_CONTRACTS.md §Cart structure.
     * Uses nested summary object instead of flat financial fields.
     */
    private CartResponse buildCartResponse(Cart cart) {
        BigDecimal subtotal = cart.getTotalPrice() != null ? cart.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal discount = getDiscount(cart);
        BigDecimal total = subtotal.subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        // Build nested summary per Vision spec
        CartSummary summary = CartSummary.builder()
                .subtotal(subtotal)
                .discount(discount)
                .total(total)
                .build();

        return CartResponse.builder()
                .id(cart.getId())
                .cartItems(getCartItems(cart))
                .summary(summary)
                .voucher(getVoucher(cart))
                .itemCount(cart.getCartItems() != null 
                    ? cart.getCartItems().stream().mapToInt(item -> item.getQuantity()).sum() 
                    : 0)
                .build();
    }

    private List<CartItemForCartResponse> getCartItems(Cart cart) {
        if (cart.getCartItems() == null) return List.of();

        return cart.getCartItems().stream()
                .map(cartItemMapper::toCartItemForCartResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartResponse applyVoucherToCart(Long userId, String voucherCode) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        Voucher voucher = voucherRepository.findByCode(voucherCode)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        // --- VALIDATION LOGIC ---
        if (!voucher.getIsActive()) {
            throw new AppException(ErrorCode.VOUCHER_NOT_FOUND); // Nên có mã lỗi riêng: VOUCHER_INACTIVE
        }

        if (voucher.getValidTo() != null && voucher.getValidTo().isBefore(java.time.LocalDateTime.now())) {
            throw new AppException(ErrorCode.VOUCHER_EXPIRED); // Nên có mã lỗi riêng
        }

        // Check điều kiện giá trị tối thiểu
        // Lưu ý: cart.getTotalPrice() ở đây là Subtotal
        if (voucher.getMinOrderValue() != null && cart.getTotalPrice().compareTo(voucher.getMinOrderValue()) < 0) {
            throw new AppException(ErrorCode.VOUCHER_MIN_ORDER_VALUE_NOT_MET);
        }
        // ------------------------

        // 2. Lưu Voucher vào Cart
        cart.setVoucher(voucher);
        Cart savedCart = cartRepository.save(cart);

        // 3. Quan trọng: Gọi hàm helper để tính toán lại toàn bộ (Tax, Ship, Total - Discount)
        return buildCartResponse(savedCart);
    }

    @Transactional
    public CartResponse removeVoucherFromCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        cart.setVoucher(null); // Gỡ voucher
        Cart savedCart = cartRepository.save(cart);

        // Tự động tính lại giá gốc (không trừ discount nữa)
        return buildCartResponse(savedCart);
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
