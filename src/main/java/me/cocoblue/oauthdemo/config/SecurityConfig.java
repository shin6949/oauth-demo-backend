package me.cocoblue.oauthdemo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final ClientRegistrationRepository clientRegistrationRepository;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .headers(headers -> headers
            .frameOptions(FrameOptionsConfig::disable)
        )
        .oauth2Login(oauth2 -> oauth2
            .tokenEndpoint(tokenEndpoint ->
                tokenEndpoint.accessTokenResponseClient(accessTokenResponseClient())
            )
            .userInfoEndpoint(userInfoEndpoint ->
                userInfoEndpoint.userService(userService())
            )
        );

    return http.build();
  }

  @Bean
  public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
    DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();

    client.setRequestEntityConverter(new OAuth2AuthorizationCodeGrantRequestEntityConverter() {
      @Override
      public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest oauth2Request) {
        return withUserAgent(super.convert(oauth2Request));
      }
    });

    return client;
  }

  @Bean
  public OAuth2UserService<OAuth2UserRequest, OAuth2User> userService() {
    DefaultOAuth2UserService service = new DefaultOAuth2UserService();

    service.setRequestEntityConverter(new OAuth2UserRequestEntityConverter() {
      @Override
      public RequestEntity<?> convert(OAuth2UserRequest userRequest) {
        return withUserAgent(super.convert(userRequest));
      }
    });

    return service;
  }

  private RequestEntity<?> withUserAgent(RequestEntity<?> requestEntity) {
    // 기존 RequestEntity의 메서드, URL, 헤더, 바디 정보를 사용하여 새로 구성
    return new RequestEntity<>(
        requestEntity.getBody(), // 기존 바디 사용
        addUserAgentHeader(requestEntity.getHeaders()), // User-Agent 헤더 추가
        requestEntity.getMethod(), // 기존 메서드 사용 (GET, POST 등)
        requestEntity.getUrl() // 기존 URL 사용
    );
  }

  private HttpHeaders addUserAgentHeader(HttpHeaders headers) {
    HttpHeaders newHeaders = new HttpHeaders();
    newHeaders.putAll(headers); // 기존 헤더 복사
    newHeaders.set("User-Agent", "DISCORD_BOT_USER_AGENT"); // User-Agent 헤더 추가
    return newHeaders;
  }
}