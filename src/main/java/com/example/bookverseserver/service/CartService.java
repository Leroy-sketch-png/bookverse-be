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

    // 1. Method chính: Rất ngắn gọn
    public CartResponse getCartByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Dùng orElseGet để xử lý logic: "Nếu có thì lấy, không có thì tạo mới"
        // Method findByUserId này chính là cái @Query tối ưu JOIN FETCH ở Repo
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .totalPrice(BigDecimal.ZERO)
                            .build();
                    return cartRepository.save(newCart);
                });

        // Gọi hàm helper để tính toán Tax, Ship, Total và map DTO
        return buildCartResponse(cart);
    }

    // 2. Helper Method: Nơi tập trung logic tính tiền (đã gửi ở step trước)
    private CartResponse buildCartResponse(Cart cart) {
        BigDecimal subtotal = cart.getTotalPrice() != null ? cart.getTotalPrice() : BigDecimal.ZERO;

        // --- CÁC LOGIC CÒN THIẾU TRONG CODE CŨ ---
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.09")); // Tax 9%

        BigDecimal shipping = (subtotal.compareTo(new BigDecimal("50.00")) > 0)
                ? BigDecimal.ZERO : new BigDecimal("5.00"); // Ship logic

        BigDecimal discount = getDiscount(cart);

        // Tính tổng cuối: Subtotal + Tax + Ship - Discount
        BigDecimal total = subtotal.add(tax).add(shipping).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;
        // ------------------------------------------

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .cartItems(getCartItems(cart))
                .subtotal(subtotal)
                .tax(tax)           // Mới
                .shipping(shipping) // Mới
                .discount(discount)
                .total(total)       // Mới (Số tiền user phải trả)
                .voucher(getVoucher(cart))
                .itemCount(cart.getCartItems() != null ? cart.getCartItems().size() : 0)
                .build();
    }

    // 3. Helper lấy items (giữ nguyên logic của bạn nhưng viết ngắn hơn)
    private Set<CartItemForCartResponse> getCartItems(Cart cart) {
        if (cart.getCartItems() == null) return Set.of();

        return cart.getCartItems().stream()
                .map(cartItemMapper::toCartItemForCartResponse)
                .collect(Collectors.toSet());
    }

    @Transactional // Nên thêm để đảm bảo tính nhất quán khi save
    public CartResponse applyVoucherToCart(Long userId, String voucherCode) {
        // 1. Dùng query tối ưu (JOIN FETCH) để lấy items luôn
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
