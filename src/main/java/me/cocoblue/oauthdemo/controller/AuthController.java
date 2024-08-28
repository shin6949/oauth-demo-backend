package me.cocoblue.oauthdemo.controller;

import lombok.RequiredArgsConstructor;
import me.cocoblue.oauthdemo.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

  private final JwtTokenProvider jwtTokenProvider;

  @PostMapping("/token/refresh")
  public ResponseEntity<?> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
    // Refresh Token 검증
    if (jwtTokenProvider.validateToken(refreshToken)) {
      String username = jwtTokenProvider.getUsername(refreshToken);

      // SecurityContext에 저장할 새로운 인증 객체 생성
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      // 새로운 Access Token 생성
      String newAccessToken = jwtTokenProvider.createAccessToken(authentication);

      // 새로운 Access Token 반환
      return ResponseEntity.ok().body("{\"accessToken\": \"" + newAccessToken + "\"}");
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Refresh Token");
    }
  }
}
