package com.medina.heritage.userauth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) 
public class CachedBodyFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (request.getRequestURI().startsWith("/api/webhooks/clerk") &&
        "POST".equalsIgnoreCase(request.getMethod())) {

      CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
      log.debug("Request body cached for webhook: {}", request.getRequestURI());
      filterChain.doFilter(cachedRequest, response);
    } else {
      filterChain.doFilter(request, response);
    }
  }
}