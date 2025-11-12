// security/JwtAuthFilter.java
package com.example.healthmonitoring.security;
import com.example.healthmonitoring.auth.repo.UserRepository;
import jakarta.servlet.*; import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter; import java.io.IOException; import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwt; private final UserRepository users;
  public JwtAuthFilter(JwtService jwt, UserRepository users){ this.jwt=jwt; this.users=users; }


  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    var h = req.getHeader("Authorization");
    if (h!=null && h.startsWith("Bearer ")) {
      try {
        var email = jwt.subject(h.substring(7));
        users.findByEmail(email).ifPresent(u -> {
          var token = new UsernamePasswordAuthenticationToken(u, null, List.of());
          SecurityContextHolder.getContext().setAuthentication(token);
        });
      } catch (Exception ignored) {}
    }
    chain.doFilter(req, res);
    
  }
}

