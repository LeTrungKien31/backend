package com.example.healthmonitoring.ping;

import org.springframework.web.bind.annotation.*;

import com.example.healthmonitoring.auth.entity.User;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v1/ping")
public class PingController {
  @GetMapping
  public String open() {
    return "open ok";
  }

  @GetMapping("/me")
  public String me(@AuthenticationPrincipal User me) {
    return "hello " + me.getFullName();
  }
}
