package com.dnd.dto;

import java.io.Serializable;

public record UserInfo(
        Long id,
        String username,
        String email,
        String role
) implements Serializable {}
