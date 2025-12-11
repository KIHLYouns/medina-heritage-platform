package com.medina.heritage.userauth.config;

import com.svix.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ClerkWebhookSecurityInterceptor implements HandlerInterceptor {

  @Value("${clerk.webhook.secret}")
  private String clerkWebhookSecret;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    if (!request.getRequestURI().startsWith("/api/webhooks/clerk")) {
      return true;
    }

    try {
      String svixId = request.getHeader("svix-id");
      String svixTimestamp = request.getHeader("svix-timestamp");
      String svixSignature = request.getHeader("svix-signature");

      if (svixId == null || svixTimestamp == null || svixSignature == null) {
        log.warn("Missing Clerk webhook headers");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing webhook headers");
        return false;
      }

 
      String body;
      if (request instanceof CachedBodyHttpServletRequest) {
        body = ((CachedBodyHttpServletRequest) request).getBody();
      } else {
        log.error("Request is not wrapped with CachedBodyHttpServletRequest!");
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return false;
      }

      HttpHeaders headers = HttpHeaders.of(
          Map.of(
              "svix-id", List.of(svixId),
              "svix-timestamp", List.of(svixTimestamp),
              "svix-signature", List.of(svixSignature)),
          (a, b) -> true);

      Webhook webhook = new Webhook(clerkWebhookSecret);
      webhook.verify(body, headers);

      log.info("Clerk webhook signature validated successfully");
      return true;

    } catch (Exception e) {
      log.error("Failed to validate Clerk webhook signature: {}", e.getMessage());
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid webhook signature");
      return false;
    }
  }
}