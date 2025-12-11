package com.medina.heritage.userauth.controller;

import com.medina.heritage.userauth.dto.request.ClerkWebhookRequest;
import com.medina.heritage.userauth.service.ClerkWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Webhook endpoint pour recevoir les événements de Clerk.
 * Events: user.created
 */
@Slf4j
@RestController
@RequestMapping("/api/webhooks/clerk")
@RequiredArgsConstructor
public class ClerkWebhookController {

  private final ClerkWebhookService clerkWebhookService;

  @PostMapping
  public ResponseEntity<Void> handleClerkWebhook(@RequestBody ClerkWebhookRequest request) {
    log.info("Received Clerk webhook: type={}, userId={}",
        request.getType(),
        request.getData() != null ? request.getData().getClerkUserId() : "null");

    if (request.getData() != null) {
      log.debug("Webhook data - emailAddresses array length: {}",
          request.getData().getEmailAddresses() != null ? request.getData().getEmailAddresses().length : 0);
      log.debug("Primary email: {}", request.getData().getPrimaryEmail());
      log.debug("First name: {}, Last name: {}", request.getData().getFirstName(), request.getData().getLastName());
    }

    try {
      clerkWebhookService.processWebhook(request);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      log.error("Error processing Clerk webhook", e);
      return ResponseEntity.ok().build();
    }
  }
}
