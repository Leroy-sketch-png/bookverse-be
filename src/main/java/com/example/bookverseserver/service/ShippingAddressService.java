package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.ShippingAddress.ShippingAddressRequest;
import com.example.bookverseserver.dto.response.ShippingAddress.ShippingAddressResponse;
import com.example.bookverseserver.entity.User.ShippingAddress;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.ShippingAddressMapper;
import com.example.bookverseserver.repository.ShippingAddressRepository;
import com.example.bookverseserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShippingAddressService {

    ShippingAddressRepository shippingAddressRepository;
    ShippingAddressMapper shippingAddressMapper;
    UserRepository userRepository;

    @Transactional
    public ShippingAddressResponse createShippingAddress(Long userId, ShippingAddressRequest request) {
        log.info("Creating shipping address for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            shippingAddressRepository.resetDefaultAddress(userId);
        }

        ShippingAddress shippingAddress = shippingAddressMapper.toShippingAddress(request);
        shippingAddress.setUser(user);

        // Nếu chưa có địa chỉ nào, đặt làm mặc định
        List<ShippingAddress> existingAddresses = shippingAddressRepository.findByUserId(userId);
        if (existingAddresses.isEmpty()) {
            shippingAddress.setIsDefault(true);
        }

        ShippingAddress savedAddress = shippingAddressRepository.save(shippingAddress);
        log.info("Shipping address created with id: {}", savedAddress.getId());

        return shippingAddressMapper.toShippingAddressResponse(savedAddress);
    }

    public List<ShippingAddressResponse> getShippingAddressesByUserId(Long userId) {
        log.info("Getting shipping addresses for user: {}", userId);

        List<ShippingAddress> addresses = shippingAddressRepository.findByUserId(userId);

        return addresses.stream()
                .map(shippingAddressMapper::toShippingAddressResponse)
                .collect(Collectors.toList());
    }

    public ShippingAddressResponse getShippingAddressById(Long userId, Long addressId) {
        log.info("Getting shipping address {} for user: {}", addressId, userId);

        ShippingAddress address = shippingAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_ADDRESS_NOT_FOUND));

        return shippingAddressMapper.toShippingAddressResponse(address);
    }

    @Transactional
    public ShippingAddressResponse updateShippingAddress(Long userId, Long addressId, ShippingAddressRequest request) {
        log.info("Updating shipping address {} for user: {}", addressId, userId);

        ShippingAddress address = shippingAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_ADDRESS_NOT_FOUND));

        // Nếu đặt làm mặc định, reset các địa chỉ mặc định khác
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            shippingAddressRepository.resetDefaultAddress(userId);
        }

        shippingAddressMapper.updateShippingAddress(address, request);
        ShippingAddress savedAddress = shippingAddressRepository.save(address);

        log.info("Shipping address updated: {}", savedAddress.getId());
        return shippingAddressMapper.toShippingAddressResponse(savedAddress);
    }

    @Transactional
    public void deleteShippingAddress(Long userId, Long addressId) {
        log.info("Deleting shipping address {} for user: {}", addressId, userId);

        ShippingAddress address = shippingAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_ADDRESS_NOT_FOUND));

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());

        shippingAddressRepository.delete(address);

        // Nếu xóa địa chỉ mặc định, đặt địa chỉ đầu tiên làm mặc định
        if (wasDefault) {
            List<ShippingAddress> remainingAddresses = shippingAddressRepository.findByUserId(userId);
            if (!remainingAddresses.isEmpty()) {
                ShippingAddress firstAddress = remainingAddresses.get(0);
                firstAddress.setIsDefault(true);
                shippingAddressRepository.save(firstAddress);
            }
        }

        log.info("Shipping address deleted: {}", addressId);
    }

    @Transactional
    public ShippingAddressResponse setDefaultAddress(Long userId, Long addressId) {
        log.info("Setting default address {} for user: {}", addressId, userId);

        ShippingAddress address = shippingAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_ADDRESS_NOT_FOUND));

        // Reset tất cả địa chỉ mặc định
        shippingAddressRepository.resetDefaultAddress(userId);

        // Đặt địa chỉ này làm mặc định
        address.setIsDefault(true);
        ShippingAddress savedAddress = shippingAddressRepository.save(address);

        log.info("Default address set: {}", savedAddress.getId());
        return shippingAddressMapper.toShippingAddressResponse(savedAddress);
    }

    public ShippingAddressResponse getDefaultAddress(Long userId) {
        log.info("Getting default address for user: {}", userId);

        ShippingAddress address = shippingAddressRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPPING_ADDRESS_NOT_FOUND));

        return shippingAddressMapper.toShippingAddressResponse(address);
    }
}

