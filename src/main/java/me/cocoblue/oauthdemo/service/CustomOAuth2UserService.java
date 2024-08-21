package me.cocoblue.oauthdemo.service;

import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.oauthdemo.domain.TokenEntity;
import me.cocoblue.oauthdemo.domain.UserEntity;
import me.cocoblue.oauthdemo.domain.UserRepository;
import me.cocoblue.oauthdemo.domain.TokenRepository;
import me.cocoblue.oauthdemo.dto.DiscordOAuth2UserDto;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.debug("Discord OAuth2 로그인 요청 진입");
        log.debug("Access Token: {}", userRequest.getAccessToken().getTokenValue());

        // DefaultOAuth2UserService를 통해 OAuth2User 정보 가져오기
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // Discord OAuth2 API에서 가져온 사용자 정보 (id, username 등)
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // DiscordOAuth2UserDto로 변환 (DTO 클래스)
        DiscordOAuth2UserDto discordUser = new DiscordOAuth2UserDto(attributes);

        // 사용자 정보를 DB에서 조회 또는 저장
        UserEntity userEntity = getUser(discordUser);

        // 리프레시 토큰 저장
        saveTokens(userEntity, userRequest, oAuth2User);

        // DefaultOAuth2User로 변환하여 반환
        return new DefaultOAuth2User(
            oAuth2User.getAuthorities(),
            attributes,
            "id" // Discord의 고유 id를 사용
        );
    }

    private void saveTokens(UserEntity user, OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        // Principal name (OAuth2User의 고유 식별자)를 사용하여 리프레시 토큰 가져오기
        String principalName = oAuth2User.getName();

        // OAuth2AuthorizedClient를 사용하여 Refresh Token을 가져옴
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
            userRequest.getClientRegistration().getRegistrationId(),
            principalName // Principal name을 사용
        );

        // 리프레시 토큰을 authorizedClient에서 추출
        String refreshToken = null;
        if (authorizedClient != null && authorizedClient.getRefreshToken() != null) {
            refreshToken = authorizedClient.getRefreshToken().getTokenValue();
        }

        // 액세스 토큰과 리프레시 토큰을 저장하는 로직
        TokenEntity tokenEntity = TokenEntity.builder()
            .user(user)
            .accessToken(userRequest.getAccessToken().getTokenValue())
            .refreshToken(refreshToken) // 추출한 리프레시 토큰 저장
            .build();

        tokenRepository.save(tokenEntity);
    }

    private UserEntity getUser(DiscordOAuth2UserDto discordUser) {
        // Discord의 id로 사용자 조회
        return userRepository.findById(discordUser.getId())
            .orElseGet(() -> createUser(discordUser));
    }

    private UserEntity createUser(DiscordOAuth2UserDto discordUser) {
        // 새로운 사용자를 생성하고 DB에 저장
        UserEntity newUser = discordUser.toEntity();
        return userRepository.save(newUser);
    }
}