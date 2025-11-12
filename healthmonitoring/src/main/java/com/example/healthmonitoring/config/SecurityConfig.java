// security/SecurityConfig.java
package com.example.healthmonitoring.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import com.example.healthmonitoring.security.JwtAuthFilter;

import java.util.List;

@Configuration
public class SecurityConfig {
  private final JwtAuthFilter jwt; public SecurityConfig(JwtAuthFilter jwt){ this.jwt = jwt; }
  @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }

  @Bean SecurityFilterChain filter(HttpSecurity http) throws Exception {
    http.csrf(csrf->csrf.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(reg->reg
          .requestMatchers("/swagger-ui/**","/v3/api-docs/**","/api/v1/auth/**").permitAll()
          .requestMatchers("/api/v1/ping","/api/v1/ping/**").permitAll()
          .anyRequest().authenticated())
        .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
  @Bean CorsConfigurationSource cors(){
    var c=new CorsConfiguration(); c.setAllowedOrigins(List.of("*"));
    c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
    c.setAllowedHeaders(List.of("*"));
    var s=new UrlBasedCorsConfigurationSource(); s.registerCorsConfiguration("/**",c); return s;
  }
}

