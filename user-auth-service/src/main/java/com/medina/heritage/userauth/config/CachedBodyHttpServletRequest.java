package com.medina.heritage.userauth.config;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Wrapper pour HttpServletRequest qui permet de lire le body plusieurs fois.
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

  private byte[] cachedBody;

  public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
    super(request);
    this.cachedBody = request.getInputStream().readAllBytes();
  }

  @Override
  public ServletInputStream getInputStream() {
    return new ServletInputStream() {
      private final ByteArrayInputStream inputStream = new ByteArrayInputStream(cachedBody);

      @Override
      public int read() {
        return inputStream.read();
      }

      @Override
      public boolean isFinished() {
        return inputStream.available() == 0;
      }

      @Override
      public boolean isReady() {
        return true;
      }

      @Override
      public void setReadListener(ReadListener readListener) {
      }
    };
  }

  @Override
  public BufferedReader getReader() {
    return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedBody)));
  }

  public String getBody() {
    return new String(cachedBody);
  }
}
