package com.example.vehicleverification.infrastructure.security;

import org.springframework.stereotype.Component;

import com.example.vehicleverification.infrastructure.config.JwtProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

// トークン生成・検証・ユーザ名抽出
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    // SecretKeyはスレッドセーフなので、インスタンス変数として保持して使い回す
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties jwtProperties) {

        this.jwtProperties = jwtProperties;
        // SecretKeyを生成する際に、文字列をバイト配列に変換する
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

    }

    // HMAC-SHA256で署名したJWTを生成
    // 有効期限をJwtPropertiesから取得
    public String generateToken(String username) {

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .subject(username) // sub 主題
                .issuedAt(new Date(now)) // iat 発行時刻
                .expiration(new Date(now + jwtProperties.getExpiration())) // exp 有効期限
                .signWith(key, Jwts.SIG.HS256) // alg を明示
                .compact(); // 文字列化して返す

    }

    // 署名検証・有効期限検証
    // 例外はcatchして返す
    public boolean validateToken(String token) {
        try {

            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;

        } catch (JwtException | IllegalArgumentException e) {

            // 例外はログに残さずfalseを返す
            return false;

        }

    }

    // usernameをJWTから抽出
    public String getUsernameFromToken(String token) {

        // チェーンでsubを取得する
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();

    }

}
