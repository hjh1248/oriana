package com.oriana.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String name;            // 프론트 user.name
    private int level;              // 프론트 user.level
    private int points;             // 프론트 user.points
    private int nextLevelPoints;    // 프론트 user.nextLevelPoints
}