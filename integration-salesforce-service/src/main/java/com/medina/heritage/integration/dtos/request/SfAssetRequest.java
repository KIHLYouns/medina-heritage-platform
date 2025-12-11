package com.medina.heritage.integration.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SfAssetRequest {

    // Champs standards Salesforce
    private String Name;
    private String AccountId; // L'ID par défaut
    private String LocationId; // L'ID obtenu à l'étape A

    // Champs personnalisés (Custom Fields)
    @JsonProperty("ExternalIdentifier") // Ou ton nom API exact (ex: Code_Externe__c)
    private String externalCode;

    @JsonProperty("Image_Url__c") // Ton champ custom pour l'image
    private String imageUrl;
}