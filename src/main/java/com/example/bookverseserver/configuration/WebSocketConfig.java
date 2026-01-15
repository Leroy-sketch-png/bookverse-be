package com.example.bookverseserver.configuration;

import com.example.bookverseserver.service.NimbusJwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;

/**
 * WebSocket configuration for real-time messaging.
 * Uses STOMP protocol over WebSocket with JWT authentication.
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final NimbusJwtService jwtService;
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint - clients connect here
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS(); // Fallback for browsers without WebSocket support
    }
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple in-memory message broker
        // /topic - for broadcast messages (e.g., conversation messages)
        // /queue - for user-specific messages (private notifications)
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Prefix for messages FROM client TO server
        registry.setApplicationDestinationPrefixes("/app");
        
        // Prefix for user-specific destinations
        registry.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor == null) {
                    return message;
                }
                
                // Authenticate on CONNECT
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    log.debug("WebSocket CONNECT received, Authorization header present: {}", 
                        StringUtils.hasText(authHeader));
                    
                    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            // Validate token and extract user ID
                            if (jwtService.validateToken(token)) {
                                Long userId = jwtService.extractUserId(token);
                                String username = jwtService.extractUsername(token);
                                
                                // Create authentication with user ID as principal name
                                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                    userId.toString(), 
                                    null, 
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                                );
                                
                                accessor.setUser(authentication);
                                log.info("WebSocket user authenticated: {} (ID: {})", username, userId);
                            }
                        } catch (Exception e) {
                            log.error("WebSocket authentication failed: {}", e.getMessage());
                        }
                    } else {
                        log.warn("WebSocket CONNECT without valid Authorization header");
                    }
                }
                
                // Set authentication context for all message types
                Authentication authentication = (Authentication) accessor.getUser();
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                
                return message;
            }
        });
    }
}
