package com.example.healthmonitoring.auth.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.healthmonitoring.auth.repo.UserRepository;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserRepository userRepo;
    public UserController(UserRepository userRepo){ this.userRepo = userRepo; }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication a){
        var u = userRepo.findByEmail(a.getName()).orElseThrow();
        return Map.of("id", u.getId(), "fullname", u.getFullName(), "email", u.getEmail());
    }
}
