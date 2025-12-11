package com.medina.heritage.integration.dtos.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SalesforceUpsertResponse {
  private String id;
  private boolean success;
  private boolean created;
}