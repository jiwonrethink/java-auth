package jwt;

import error.ErrorCode;
import error.exception.JwtException;
import util.RedisUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil implements InitializingBean {
    @Value("${jwt.issuer}")
    private String issuer;
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenValidityInSeconds;
    @Value("${jwt.access-token-validity-in-seconds}")
    private Long accessTokenValidityInSeconds;
    private Key key;
    private final RedisUtil redisUtil;

    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Authentication authentication, String userUuid) {
        return issueToken(authentication,
                accessTokenValidityInSeconds * 1000,
                userUuid);
    }

    public String createRefreshToken(Authentication authentication, String userUuid) {
        return issueToken(authentication,
                refreshTokenValidityInSeconds * 1000,
                userUuid + ":refresh");
    }

    public Boolean validateToken(String token, Boolean isRefreshToken) throws JwtException {
        try {
            String userUuid = getUserUuidFromToken(token);
            String userToken = isRefreshToken
                    ? redisUtil.getValue(userUuid + ":refresh")
                    : redisUtil.getValue(userUuid);

            if (userToken == null) {
                throw new JwtException(ErrorCode.JWT_EXPIRED);
            }
            if (!userToken.equals(token)) {
                throw new JwtException(ErrorCode.INVALID_JWT);
            }
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

            return true;
        } catch (SignatureException | MalformedJwtException | IllegalArgumentException e) {
            throw new JwtException(ErrorCode.INVALID_JWT);
        } catch (ExpiredJwtException e) {
            throw new JwtException(ErrorCode.JWT_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new JwtException(ErrorCode.NOT_SUPPORT_JWT);
        }
    }

    public Boolean deleteToken(UUID userUuid) {
        boolean isDelToken = false;
        String accessTokenKey = userUuid.toString();
        String refreshTokenKey = userUuid + ":refresh";

        if (redisUtil.delValue(accessTokenKey)
                && redisUtil.delValue(refreshTokenKey)) {
            isDelToken = true;
        }

        return isDelToken;
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getAgentIdFromToken(String token) {
        return String.valueOf(getAllClaims(token).get("agentId"));
    }

    public String getUserUuidFromToken(String token) {
        return String.valueOf(getAllClaims(token).get("userUuid"));
    }

    public String getRefreshToken(String key) {
        return redisUtil.getValue(key + ":refresh");
    }

    private String issueToken(Authentication authentication, long validityInMilliSeconds, String key) {
        Date iat = new Date(); // iat: 토큰이 발급된 시간 (issued at)
        Date exp = new Date(iat.getTime() + validityInMilliSeconds); // exp: 토큰 만료 시간
        String agentId = ((JwtPrincipal) authentication.getPrincipal()).getAgentId();
        String userUuid = ((JwtPrincipal) authentication.getPrincipal()).getUserUuid();

        String token = Jwts.builder()
                .setSubject(userUuid)
                .setIssuer(issuer)
                .claim("agentId", agentId)
                .claim("userUuid", userUuid)
                .setIssuedAt(iat)
                .setExpiration(exp)
                .signWith(this.key, SignatureAlgorithm.HS256)
                .compact();

        redisUtil.setValue(key, token, validityInMilliSeconds);
        return token;
    }

    public Authentication getAuthentication(String token) {
        String agentId = getAgentIdFromToken(token);
        String userUuid = getUserUuidFromToken(token);

        JwtPrincipal principal = new JwtPrincipal(agentId, userUuid);

        return new UserAuthentication(principal, token, null);
    }
}
