package com.medina.heritage.userauth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) 
public class ClerkWebhookRequest {

  private String type;
  private ClerkData data;

  @JsonProperty("object")
  private String object;

  @JsonProperty("timestamp")
  private Long timestamp;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true) 
  public static class ClerkData {
    @JsonProperty("id")
    private String clerkUserId;

    @JsonProperty("email_addresses")
    private EmailAddress[] emailAddresses;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("phone_numbers")
    private PhoneNumber[] phoneNumbers;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("created_at")
    private Long createdAt;

    public String getPrimaryEmail() {
      if (emailAddresses != null && emailAddresses.length > 0) {
        for (EmailAddress email : emailAddresses) {
          if (email.isPrimary()) {
            return email.getEmailAddress();
          }
        }
        return emailAddresses[0].getEmailAddress();
      }
      return null;
    }

    public String getPrimaryPhone() {
      if (phoneNumbers != null && phoneNumbers.length > 0) {
        for (PhoneNumber phone : phoneNumbers) {
          if (phone.isPrimary()) {
            return phone.getPhoneNumber();
          }
        }
        return phoneNumbers[0].getPhoneNumber();
      }
      return null;
    }

    public String getProfileImage() {
      return profileImageUrl != null ? profileImageUrl : imageUrl;
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class EmailAddress {
    private String id;

    @JsonProperty("email_address")
    private String emailAddress;

    private Verification verification;

    @JsonProperty("primary")
    private Boolean primary = false;

    public boolean isPrimary() {
      return primary != null && primary;
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PhoneNumber {
    private String id;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private Verification verification;

    @JsonProperty("primary")
    private Boolean primary = false;

    public boolean isPrimary() {
      return primary != null && primary;
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Verification {
    private String status;
    private String strategy;
  }
}