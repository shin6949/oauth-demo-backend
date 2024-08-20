package me.cocoblue.oauthdemo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user", indexes = @Index(name = "username_idx", columnList = "username"))
public class UserEntity implements Serializable {
  @EmbeddedId
  private UserId userId;

  @Column(name = "username", nullable = false)
  private String username;

  @Column(name = "nickname", columnDefinition = "DEFAULT null")
  private String nickname;

  @Column(name = "profile_image", columnDefinition = "DEFAULT null")
  private String profileImage;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", columnDefinition = "VARCHAR(255) DEFAULT 'USER'")
  private UserRole userRole;

  @PrePersist
  @PreUpdate
  public void prePersistOrUpdate() {
    if (this.nickname != null && this.nickname.isBlank()) {
      this.nickname = null;
    }

    if(this.userRole == null) {
      this.userRole = UserRole.USER;
    }
  }
}
