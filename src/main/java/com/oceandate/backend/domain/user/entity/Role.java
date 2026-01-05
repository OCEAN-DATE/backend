package com.oceandate.backend.domain.user.entity;

public enum Role {
    USER, ADMIN;

    public static String toAuthority(Role role) {
        return "ROLE_" + role.name();
    }
}
