package com.medina.heritage.userauth.service;

import com.medina.heritage.events.user.UserCreatedByClerkEvent;
import com.medina.heritage.userauth.dto.request.ClerkWebhookRequest;
import com.medina.heritage.userauth.messaging.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ClerkWebhookService {

  private final UserEventPublisher userEventPublisher;

  public void processWebhook(ClerkWebhookRequest request) {
    if ("user.created".equals(request.getType())) {
      handleUserCreated(request);
    } else {
      log.debug("Webhook type not handled: {}", request.getType());
    }
  }

  private void handleUserCreated(ClerkWebhookRequest request) {
    ClerkWebhookRequest.ClerkData data = request.getData();
    log.info("Processing user.created webhook for Clerk user: {}", data.getClerkUserId());

    String primaryEmail = data.getPrimaryEmail();
    log.debug("Extracted primary email: {} from {} email addresses",
        primaryEmail,
        data.getEmailAddresses() != null ? data.getEmailAddresses().length : 0);

    if (primaryEmail == null) {
      log.warn("WARNING: Primary email is null for Clerk user: {}. Email addresses array length: {}",
          data.getClerkUserId(),
          data.getEmailAddresses() != null ? data.getEmailAddresses().length : 0);
    }

    UserCreatedByClerkEvent event = UserCreatedByClerkEvent.builder()
        .clerkUserId(data.getClerkUserId())
        .email(primaryEmail)
        .firstName(data.getFirstName())
        .lastName(data.getLastName())
        .phoneNumber(data.getPrimaryPhone())
        .profileImageUrl(data.getProfileImageUrl())
        .build();

    try {
      userEventPublisher.publishUserCreatedByClerk(event);
      log.info("Published UserCreatedByClerkEvent for Clerk user: {}, email: {}", data.getClerkUserId(), primaryEmail);
    } catch (Exception e) {
      log.error("Failed to publish UserCreatedByClerkEvent for Clerk user: {}. Error: {}",
          data.getClerkUserId(), e.getMessage(), e);
      throw new RuntimeException("Failed to publish event to RabbitMQ", e);
    }
  }
}
