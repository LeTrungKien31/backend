// security/JwtService.java
package com.example.healthmonitoring.security;
import io.jsonwebtoken.*; import io.jsonwebtoken.io.Decoders; import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import java.security.Key; import java.util.*;

@Service
public class JwtService {
  private final Key key; private final int expHours;
  public JwtService(@Value("${app.jwt.secret}") String secret,
                    @Value("${app.jwt.expirationHours}") int expHours){
    this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)); // decode Base64
    this.expHours = expHours;
  }
  public String generate(String sub, Map<String,Object> claims){
    var now=new Date(); var exp=new Date(now.getTime()+expHours*3600_000L);
    return Jwts.builder().setSubject(sub).addClaims(claims).setIssuedAt(now).setExpiration(exp)
      .signWith(key, SignatureAlgorithm.HS256).compact();
  }
  public String subject(String token){
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
  }
}
