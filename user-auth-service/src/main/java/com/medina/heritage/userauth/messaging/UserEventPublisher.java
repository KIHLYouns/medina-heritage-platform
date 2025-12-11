package com.medina.heritage.userauth.messaging;

import com.medina.heritage.events.user.UserCreatedEvent;
import com.medina.heritage.events.user.UserCreatedByClerkEvent;
import com.medina.heritage.events.user.UserDeletedEvent;
import com.medina.heritage.events.user.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

/**
 * Service for publishing user-related events to the message broker.
 * Uses Spring Cloud Stream's StreamBridge for dynamic event publishing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final StreamBridge streamBridge;

    private static final String USER_CREATED_BINDING = "userCreatedSupplier-out-0";
    private static final String USER_UPDATED_BINDING = "userUpdatedSupplier-out-0";
    private static final String USER_DELETED_BINDING = "userDeletedSupplier-out-0";
    private static final String USER_CREATED_BY_CLERK_BINDING = "userCreatedByClerkSupplier-out-0";

    /**
     * Publishes a UserCreatedEvent when a new user is registered.
     */
    public void publishUserCreated(UserCreatedEvent event) {
        event.initializeDefaults();
        log.info("Publishing UserCreatedEvent for user: {} ({})", event.getUserId(), event.getEmail());
        boolean sent = streamBridge.send(USER_CREATED_BINDING, event);
        if (sent) {
            log.debug("UserCreatedEvent sent successfully for user: {}", event.getUserId());
        } else {
            log.error("Failed to send UserCreatedEvent for user: {}", event.getUserId());
        }
    }

    /**
     * Publishes a UserUpdatedEvent when a user's profile is updated.
     */
    public void publishUserUpdated(UserUpdatedEvent event) {
        event.initializeDefaults();
        log.info("Publishing UserUpdatedEvent for user: {}", event.getUserId());
        boolean sent = streamBridge.send(USER_UPDATED_BINDING, event);
        if (sent) {
            log.debug("UserUpdatedEvent sent successfully for user: {}", event.getUserId());
        } else {
            log.error("Failed to send UserUpdatedEvent for user: {}", event.getUserId());
        }
    }

    /**
     * Publishes a UserDeletedEvent when a user is deactivated/deleted.
     */
    public void publishUserDeleted(UserDeletedEvent event) {
        event.initializeDefaults();
        log.info("Publishing UserDeletedEvent for user: {}", event.getUserId());
        boolean sent = streamBridge.send(USER_DELETED_BINDING, event);
        if (sent) {
            log.debug("UserDeletedEvent sent successfully for user: {}", event.getUserId());
        } else {
            log.error("Failed to send UserDeletedEvent for user: {}", event.getUserId());
        }
    }

    /**
     * Publishes a UserCreatedByClerkEvent when Clerk sends a webhook.
     * Cet événement sera consommé par le consumer dans ce même service.
     */
    public void publishUserCreatedByClerk(UserCreatedByClerkEvent event) {
        event.initializeDefaults();
        log.info("Publishing UserCreatedByClerkEvent for Clerk user: {}", event.getClerkUserId());
        boolean sent = streamBridge.send(USER_CREATED_BY_CLERK_BINDING, event);
        if (sent) {
            log.debug("UserCreatedByClerkEvent sent successfully for user: {}", event.getClerkUserId());
        } else {
            log.error("Failed to send UserCreatedByClerkEvent for user: {}", event.getClerkUserId());
        }
    }
}
