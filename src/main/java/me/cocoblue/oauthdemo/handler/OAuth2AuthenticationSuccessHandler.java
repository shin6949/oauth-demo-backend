package me.cocoblue.oauthdemo.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.oauthdemo.security.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Log4j2
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;

  public OAuth2AuthenticationSuccessHandler(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
    // Access Token 및 Refresh Token 생성
    String accessToken = jwtTokenProvider.createAccessToken(authentication);
    String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

    // Refresh Token을 서버에서 저장 (DB에 저장 가능)
    // 여기서는 간단히 로그로 출력하겠지만, 보통 DB 또는 Redis에 저장합니다.
    log.info("Refresh Token: {}", refreshToken);

    // Access Token과 Refresh Token을 응답 헤더로 전송 (또는 JSON으로 전송 가능)
    response.setHeader("Authorization", "Bearer " + accessToken);
    response.setHeader("Refresh-Token", refreshToken);

    // JSON 응답으로도 보낼 수 있습니다.
    response.setContentType("application/json");
    response.getWriter().write("{\"accessToken\": \"" + accessToken + "\", \"refreshToken\": \"" + refreshToken + "\"}");
  }
}