package com.example.healthmonitoring.auth.controller;
import com.example.healthmonitoring.auth.entity.User;
import com.example.healthmonitoring.auth.repo.UserRepository;
import com.example.healthmonitoring.security.JwtService;
import jakarta.validation.constraints.*;
import lombok.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final UserRepository users;
  private final PasswordEncoder pe;
  private final JwtService jwt;

  public AuthController(UserRepository u, PasswordEncoder p, JwtService j) {
    users = u;
    pe = p;
    jwt = j;
  }

  @PostMapping("/register")
  public Map<String, String> register(@RequestBody RegisterReq req) {
    if (users.findByEmail(req.getEmail()).isPresent())
      throw new RuntimeException("Email exists");
    var u = users.save(User.builder().email(req.getEmail()).password(pe.encode(req.getPassword()))
        .fullName(req.getFullName()).roles("USER").build());
    return Map.of("token", jwt.generate(u.getEmail(), Map.of("uid", u.getId().toString(), "name", u.getFullName())));
  }

  @PostMapping("/login")
  public Map<String, String> login(@RequestBody LoginReq req) {
    var u = users.findByEmail(req.getEmail()).orElseThrow(() -> new RuntimeException("Not found"));
    if (!pe.matches(req.getPassword(), u.getPassword()))
      throw new RuntimeException("Wrong password");
    return Map.of("token", jwt.generate(u.getEmail(), Map.of("uid", u.getId().toString(), "name", u.getFullName())));
  }





  @Getter
  @Setter
  static class RegisterReq {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String fullName;
  }

  @Getter
  @Setter
  static class LoginReq {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;
  }
}
