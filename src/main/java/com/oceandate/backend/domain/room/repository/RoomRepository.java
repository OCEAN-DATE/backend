package com.oceandate.backend.domain.room.repository;

import com.oceandate.backend.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
