package me.cocoblue.oauthdemo.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserId, UserEntity> {
  Optional<UserEntity> findById(UserId id);
}
