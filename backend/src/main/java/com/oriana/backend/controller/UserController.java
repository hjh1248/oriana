package com.oriana.backend.controller;

import com.oriana.backend.dto.UserResponseDto;
import com.oriana.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/users/1 호출 시 실행됨
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserInfo(@PathVariable Long id) {
        UserResponseDto userInfo = userService.findById(id);
        return ResponseEntity.ok(userInfo);
    }
}