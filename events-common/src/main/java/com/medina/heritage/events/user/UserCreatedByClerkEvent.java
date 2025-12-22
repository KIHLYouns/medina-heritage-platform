package com.medina.heritage.events.user;

import com.medina.heritage.events.base.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Event published when Clerk creates a new user.
 * Webhook from Clerk → REST endpoint → RabbitMQ → user-auth-service
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserCreatedByClerkEvent extends BaseEvent {

  public static final String EVENT_TYPE = "user.created.clerk";
  public static final String ROUTING_KEY = "user.created.clerk";

  private String clerkUserId;
  private String email;
  private String firstName;
  private String lastName;
  private String phoneNumber;
  private String profileImageUrl;

  public UserCreatedByClerkEvent initializeDefaults() {
    initializeEvent("clerk-webhook-service");
    return this;
  }
}
