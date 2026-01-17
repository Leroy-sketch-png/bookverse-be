package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.CreateConversationRequest;
import com.example.bookverseserver.dto.request.SendMessageRequest;
import com.example.bookverseserver.dto.response.ConversationDetailResponse;
import com.example.bookverseserver.dto.response.ConversationSummaryResponse;
import com.example.bookverseserver.dto.response.MessageResponse;
import com.example.bookverseserver.entity.Messaging.ChatMessage;
import com.example.bookverseserver.entity.Messaging.Conversation;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.MessageType;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.ChatMessageRepository;
import com.example.bookverseserver.repository.ConversationRepository;
import com.example.bookverseserver.repository.ListingRepository;
import com.example.bookverseserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)  // P0: Prevent LazyInitializationException & connection leaks
public class MessagingService {
    
    ConversationRepository conversationRepository;
    ChatMessageRepository chatMessageRepository;
    UserRepository userRepository;
    ListingRepository listingRepository;
    
    // ─────────────────────────────────────────────────────────────────────────
    // Conversation Operations
    // ─────────────────────────────────────────────────────────────────────────
    
    /**
     * Get all conversations for a user.
     */
    @Transactional(readOnly = true)
    public Page<ConversationSummaryResponse> getConversations(Long userId, Pageable pageable) {
        Page<Conversation> conversations = conversationRepository.findAllByParticipant(userId, pageable);
        return conversations.map(c -> toConversationSummary(c, userId));
    }
    
    /**
     * Get or create a conversation between buyer and seller.
     */
    @Transactional
    public ConversationSummaryResponse getOrCreateConversation(Long buyerId, CreateConversationRequest request) {
        // Validate seller exists
        User seller = userRepository.findById(request.getSellerId())
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Buyer can't message themselves
        if (buyerId.equals(request.getSellerId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // Check for existing conversation
        Conversation conversation = conversationRepository
            .findByBuyerAndSellerAndListing(buyerId, request.getSellerId(), request.getListingId())
            .orElseGet(() -> createNewConversation(buyerId, seller, request));
        
        // If initial message provided, send it
        if (request.getInitialMessage() != null && !request.getInitialMessage().isBlank()) {
            sendMessage(conversation.getId(), buyerId, SendMessageRequest.builder()
                .message(request.getInitialMessage())
                .type(MessageType.TEXT)
                .build());
            // Refresh conversation to get updated last message
            conversation = conversationRepository.findByIdWithDetails(conversation.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        }
        
        return toConversationSummary(conversation, buyerId);
    }
    
    private Conversation createNewConversation(Long buyerId, User seller, CreateConversationRequest request) {
        User buyer = userRepository.findById(buyerId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        Listing listing = null;
        if (request.getListingId() != null) {
            listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));
            
            // Verify the seller owns this listing
            if (!listing.getSeller().getId().equals(seller.getId())) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }
        
        String buyerName = buyer.getUserProfile() != null ? buyer.getUserProfile().getDisplayName() : buyer.getUsername();
        String sellerName = seller.getUserProfile() != null ? seller.getUserProfile().getDisplayName() : seller.getUsername();
        
        Conversation conversation = Conversation.builder()
            .buyer(buyer)
            .seller(seller)
            .buyerName(buyerName)
            .buyerAvatar(buyer.getUserProfile() != null ? buyer.getUserProfile().getAvatarUrl() : null)
            .sellerName(sellerName)
            .sellerAvatar(seller.getUserProfile() != null ? seller.getUserProfile().getAvatarUrl() : null)
            .listing(listing)
            .buyerUnreadCount(0)
            .sellerUnreadCount(0)
            .build();
        
        conversation = conversationRepository.save(conversation);
        log.info("Created new conversation {} between buyer {} and seller {}", 
            conversation.getId(), buyerId, seller.getId());
        
        return conversation;
    }
    
    /**
     * Get conversation details with messages.
     */
    @Transactional(readOnly = true)
    public ConversationDetailResponse getConversationDetail(Long conversationId, Long userId, int messageLimit) {
        Conversation conversation = conversationRepository.findByIdWithDetails(conversationId)
            .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        
        // Verify user is participant
        if (!conversation.isParticipant(userId)) {
            throw new AppException(ErrorCode.CONVERSATION_ACCESS_DENIED);
        }
        
        // Get recent messages
        Page<ChatMessage> messagesPage = chatMessageRepository.findByConversationId(
            conversationId, PageRequest.of(0, messageLimit));
        
        List<MessageResponse> messages = messagesPage.getContent().stream()
            .map(m -> toMessageResponse(m, userId))
            .toList();
        
        // Reverse to show oldest first
        messages = messages.reversed();
        
        return ConversationDetailResponse.builder()
            .id(conversation.getId())
            .otherParty(toParticipantInfo(conversation.getOtherParty(userId), conversation, userId))
            .listing(toListingPreview(conversation.getListing()))
            .order(toOrderPreview(conversation))
            .messages(messages)
            .hasMoreMessages(messagesPage.hasNext())
            .unreadCount(conversation.getUnreadCountFor(userId))
            .createdAt(conversation.getCreatedAt())
            .build();
    }
    
    /**
     * Mark conversation as read for a user.
     */
    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        
        if (!conversation.isParticipant(userId)) {
            throw new AppException(ErrorCode.CONVERSATION_ACCESS_DENIED);
        }
        
        // Mark all messages as read
        int updated = chatMessageRepository.markAsRead(conversationId, userId, LocalDateTime.now());
        
        // Clear unread count
        conversation.clearUnreadFor(userId);
        conversationRepository.save(conversation);
        
        log.debug("Marked {} messages as read in conversation {} for user {}", 
            updated, conversationId, userId);
    }
    
    /**
     * Get total unread count for a user.
     */
    @Transactional(readOnly = true)
    public int getTotalUnreadCount(Long userId) {
        return conversationRepository.countTotalUnread(userId);
    }
    
    // ─────────────────────────────────────────────────────────────────────────
    // Message Operations
    // ─────────────────────────────────────────────────────────────────────────
    
    /**
     * Get messages for a conversation (paginated).
     */
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(Long conversationId, Long userId, Pageable pageable) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        
        if (!conversation.isParticipant(userId)) {
            throw new AppException(ErrorCode.CONVERSATION_ACCESS_DENIED);
        }
        
        return chatMessageRepository.findByConversationId(conversationId, pageable)
            .map(m -> toMessageResponse(m, userId));
    }
    
    /**
     * Send a message in a conversation.
     */
    @Transactional
    public MessageResponse sendMessage(Long conversationId, Long senderId, SendMessageRequest request) {
        Conversation conversation = conversationRepository.findByIdWithDetails(conversationId)
            .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        
        if (!conversation.isParticipant(senderId)) {
            throw new AppException(ErrorCode.CONVERSATION_ACCESS_DENIED);
        }
        
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        String senderName = sender.getUserProfile() != null ? sender.getUserProfile().getDisplayName() : sender.getUsername();
        
        // Build message
        ChatMessage message = ChatMessage.builder()
            .conversation(conversation)
            .sender(sender)
            .senderName(senderName)
            .senderAvatar(sender.getUserProfile() != null ? sender.getUserProfile().getAvatarUrl() : null)
            .message(request.getMessage())
            .messageType(request.getType() != null ? request.getType() : MessageType.TEXT)
            .relatedId(request.getRelatedId())
            .build();
        
        // If sharing a listing, populate preview fields
        if (request.getType() == MessageType.LISTING_SHARE && request.getRelatedId() != null) {
            Listing listing = listingRepository.findById(request.getRelatedId())
                .orElseThrow(() -> new AppException(ErrorCode.LISTING_NOT_FOUND));
            
            message.setSharedListingTitle(listing.getBookMeta().getTitle());
            message.setSharedListingImage(listing.getPhotos().isEmpty() ? null : listing.getPhotos().get(0).getUrl());
            message.setSharedListingPrice(listing.getFinalPrice());
        }
        
        message = chatMessageRepository.save(message);
        
        // Update conversation metadata
        String preview = request.getMessage();
        if (preview.length() > 200) {
            preview = preview.substring(0, 197) + "...";
        }
        conversation.setLastMessagePreview(preview);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setLastSenderId(senderId);
        conversation.incrementUnreadFor(senderId);
        conversationRepository.save(conversation);
        
        log.info("Message sent in conversation {} by user {}", conversationId, senderId);
        
        return toMessageResponse(message, senderId);
    }
    
    // ─────────────────────────────────────────────────────────────────────────
    // Mapping Helpers
    // ─────────────────────────────────────────────────────────────────────────
    
    private ConversationSummaryResponse toConversationSummary(Conversation c, Long currentUserId) {
        User otherParty = c.getOtherParty(currentUserId);
        
        return ConversationSummaryResponse.builder()
            .id(c.getId())
            .otherParty(toParticipantInfo(otherParty, c, currentUserId))
            .listing(toListingPreview(c.getListing()))
            .lastMessage(c.getLastMessagePreview() != null ? 
                ConversationSummaryResponse.LastMessageInfo.builder()
                    .preview(c.getLastMessagePreview())
                    .sentAt(c.getLastMessageAt())
                    .isFromMe(currentUserId.equals(c.getLastSenderId()))
                    .build() : null)
            .unreadCount(c.getUnreadCountFor(currentUserId))
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .build();
    }
    
    private ConversationSummaryResponse.ParticipantInfo toParticipantInfo(User user, Conversation c, Long currentUserId) {
        if (user == null) return null;
        
        // Use denormalized names if user relation not loaded
        String name;
        String avatar;
        
        if (user.getId().equals(c.getBuyer().getId())) {
            name = c.getBuyerName() != null ? c.getBuyerName() : user.getUsername();
            avatar = c.getBuyerAvatar();
        } else {
            name = c.getSellerName() != null ? c.getSellerName() : user.getUsername();
            avatar = c.getSellerAvatar();
        }
        
        boolean isPro = user.getUserProfile() != null && Boolean.TRUE.equals(user.getUserProfile().getIsProSeller());
        
        return ConversationSummaryResponse.ParticipantInfo.builder()
            .id(user.getId())
            .name(name)
            .avatar(avatar)
            .isPro(isPro)
            .build();
    }
    
    private ConversationSummaryResponse.ListingPreview toListingPreview(Listing listing) {
        if (listing == null) return null;
        
        return ConversationSummaryResponse.ListingPreview.builder()
            .id(listing.getId())
            .title(listing.getBookMeta() != null ? listing.getBookMeta().getTitle() : "Unknown")
            .thumbnail(listing.getPhotos().isEmpty() ? null : listing.getPhotos().get(0).getUrl())
            .price(listing.getFinalPrice())
            .build();
    }
    
    private ConversationDetailResponse.OrderPreview toOrderPreview(Conversation c) {
        if (c.getOrder() == null) return null;
        
        return ConversationDetailResponse.OrderPreview.builder()
            .id(c.getOrder().getId())
            .orderNumber(c.getOrder().getOrderNumber())
            .status(c.getOrder().getStatus().name())
            .build();
    }
    
    private MessageResponse toMessageResponse(ChatMessage m, Long currentUserId) {
        return MessageResponse.builder()
            .id(m.getId())
            .conversationId(m.getConversation().getId())
            .sender(MessageResponse.SenderInfo.builder()
                .id(m.getSender().getId())
                .name(m.getSenderName())
                .avatar(m.getSenderAvatar())
                .build())
            .message(m.getMessage())
            .type(m.getMessageType())
            .relatedId(m.getRelatedId())
            .sharedListing(m.getSharedListingTitle() != null ?
                MessageResponse.SharedListingInfo.builder()
                    .id(m.getRelatedId())
                    .title(m.getSharedListingTitle())
                    .thumbnail(m.getSharedListingImage())
                    .price(m.getSharedListingPrice())
                    .build() : null)
            .isFromMe(currentUserId.equals(m.getSender().getId()))
            .readAt(m.getReadAt())
            .createdAt(m.getCreatedAt())
            .build();
    }
}
