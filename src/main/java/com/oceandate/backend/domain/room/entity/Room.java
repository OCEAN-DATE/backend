package com.oceandate.backend.domain.room.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer capacity;

    @Column(name = "base_price")
    private BigDecimal basePrice;
}
