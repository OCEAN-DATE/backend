package com.oceandate.backend.domain.user.repository;

import com.oceandate.backend.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
