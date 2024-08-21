package me.cocoblue.oauthdemo.config;

//import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import me.cocoblue.oauthdemo.service.CustomOAuth2UserService;
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
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final ClientRegistrationRepository clientRegistrationRepository;
  private final CustomOAuth2UserService customOAuth2UserService;

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
                userInfoEndpoint.userService(customOAuth2UserService)
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

  private RequestEntity<?> withUserAgent(RequestEntity<?> requestEntity) {
    return new RequestEntity<>(
        requestEntity.getBody(),
        addUserAgentHeader(requestEntity.getHeaders()),
        requestEntity.getMethod(),
        requestEntity.getUrl()
    );
  }

  private HttpHeaders addUserAgentHeader(HttpHeaders headers) {
    HttpHeaders newHeaders = new HttpHeaders();
    newHeaders.putAll(headers);
    newHeaders.set("User-Agent", "DISCORD_BOT_USER_AGENT");
    return newHeaders;
  }
}