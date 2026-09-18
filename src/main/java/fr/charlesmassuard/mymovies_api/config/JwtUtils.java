package fr.charlesmassuard.mymovies_api.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtils {

    private final Key key;
    private static final long JWT_EXPIRATION_MS = 900000; // 15 minutes
    private static final long REFRESH_EXPIRATION_MS = 2592000000L;

    public JwtUtils(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String mail) {
        return buildToken(mail, JWT_EXPIRATION_MS);
    }

    public String generateRefreshToken(String mail) {
        return buildToken(mail, REFRESH_EXPIRATION_MS);
    }

    private String buildToken(String mail, long expiration) {
        return Jwts.builder()
                .setSubject(mail)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expiration))
                .signWith(key)
                .compact();
    }

    public String getMailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            //On affiche la raison exacte de l'échec
            System.out.println("Erreur de validation du token : " + e.getMessage());
            return false;
        }
    }
}