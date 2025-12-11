package com.medina.heritage.integration.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Salesforce Contact object.
 * Used for Upsert (PATCH) operations via the Contact User_UUID__c external ID.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesforceContactRequest {

    /**
     * Contact's first name
     */
    @JsonProperty("FirstName")
    private String firstName;

    /**
     * Contact's last name
     */
    @JsonProperty("LastName")
    private String lastName;

    /**
     * Contact's email
     */
    @JsonProperty("Email")
    private String email;

    /**
     * Contact's mobile phone number
     */
    @JsonProperty("MobilePhone")
    private String mobilePhone;

    /**
     * Parent Account ID (Citizens account)
     */
    @JsonProperty("AccountId")
    private String accountId;

    /**
     * External ID - User UUID from heritage platform
     * This field is used with User_UUID__c for upsert operations
     */
    @JsonProperty("User_UUID__c")
    private String userUuid;
}
