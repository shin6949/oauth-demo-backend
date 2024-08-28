package me.cocoblue.oauthdemo.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.cocoblue.oauthdemo.domain.UserInfoEntity;

@Log4j2
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscordOAuth2UserDto {
  private String id;
  private String username;
  private String discriminator;
  private Integer flags;
  private Integer publicFlags;
  private String banner;
  private Integer accentColor;
  private String globalName;
  private String bannerColor;
  private String clan;
  private boolean mfaEnabled;
  private String locale;
  private boolean verified;
  private String email;
  private String avatar;
  private String accessToken;
  private String refreshToken;

  public DiscordOAuth2UserDto(Map<String, Object> oauthAttributes) {
    this.id = (String) oauthAttributes.get("id");
    this.username = (String) oauthAttributes.get("username");
    this.discriminator = (String) oauthAttributes.get("discriminator");
    this.flags = (Integer) oauthAttributes.get("flags");
    this.publicFlags = (Integer) oauthAttributes.get("public_flags");
    this.banner = (String) oauthAttributes.get("banner");
    this.accentColor = (Integer) oauthAttributes.get("accent_color");
    this.globalName = (String) oauthAttributes.get("global_name");
    this.bannerColor = (String) oauthAttributes.get("banner_color");
    this.clan = (String) oauthAttributes.get("clan");
    this.mfaEnabled = (boolean) oauthAttributes.get("mfa_enabled");
    this.locale = (String) oauthAttributes.get("locale");
    this.verified = (boolean) oauthAttributes.get("verified");
    this.email = (String) oauthAttributes.get("email");
    this.avatar = (String) oauthAttributes.get("avatar");
  }

  public String getBannerUrl() {
    String bannerId = getBanner();
    return String.format("https://cdn.discordapp.com/banners/%s/%s?size=1024", getId(), bannerId);
  }

  public String getProfileImageUrl() {
    String avatarId = getAvatar();
    return String.format("https://cdn.discordapp.com/avatars/%s/%s?size=480", getId(), avatarId);
  }

  public UserInfoEntity toEntity() {
    return UserInfoEntity.builder()
        .id(getId())
        .username(getUsername())
        .nickname(getGlobalName())
        .profileImage(getProfileImageUrl())
        .build();
  }
}
