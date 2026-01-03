package com.oceandate.backend.domain.user.repository;


import com.oceandate.backend.domain.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
