package oort.cloud.openmarket.auth.utils.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import oort.cloud.openmarket.auth.data.AuthToken;
import oort.cloud.openmarket.auth.data.AccessTokenPayload;
import oort.cloud.openmarket.auth.data.RefreshTokenPayload;
import oort.cloud.openmarket.common.exception.auth.ExpiredTokenException;
import oort.cloud.openmarket.common.exception.auth.InvalidTokenException;
import oort.cloud.openmarket.common.exception.enums.ErrorType;
import oort.cloud.openmarket.user.data.UserDto;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.enums.UserRole;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtManager {
    private final String CLAIM_USER_ROLE_VALUE = "userRole";
    private final JwtProperties properties;
    private final Key secretKey;
    private final Clock clock;


    public JwtManager(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.secretKey = properties.getSecretKey();
        this.clock = clock;
    }

    public String getRefreshToken(Users user, Duration duration) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .signWith(this.secretKey)
                .setSubject(String.valueOf(user.getUserId()))
                .setExpiration(getExpireDate(duration))
                .compact();
    }

    public String getAccessToken(Users user, Duration duration) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .signWith(this.secretKey)
                .setSubject(String.valueOf(user.getUserId()))
                .claim(CLAIM_USER_ROLE_VALUE, user.getUserRole())
                .setExpiration(getExpireDate(duration))
                .compact();
    }

    public AuthToken createAuthToken(Users user) {
        Duration refreshDuration = Duration.ofDays(properties.refreshTokenExpiredDay());
        Duration accessDuration = Duration.ofMinutes(properties.accessTokenExpiredMinutes());
        return AuthToken.of(
                getAccessToken(user, accessDuration),
                getRefreshToken(user, refreshDuration)
        );
    }


    /*
         access_token에서 사용자 정보 추출
         유효하지 않거나 만료된 토큰일 경우 예외 던짐
     */
    public AccessTokenPayload getAccessTokenPayload(String accessToken) {
        try {
            Claims claims = getClaims(accessToken);
            Long userId = Long.parseLong(claims.getSubject());
            String roleString = claims.get(CLAIM_USER_ROLE_VALUE, String.class);
            UserRole userRole = UserRole.valueOf(roleString);
            return AccessTokenPayload.of(userId, userRole);
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new InvalidTokenException(ErrorType.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException(ErrorType.EXPIRED_TOKEN);
        }
    }

    public RefreshTokenPayload getRefreshTokenPayload(String refreshToken) {
        try {
            Claims claims = getClaims(refreshToken);
            Long userId = Long.parseLong(claims.getSubject());
            return RefreshTokenPayload.of(userId);
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            throw new InvalidTokenException(ErrorType.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException(ErrorType.EXPIRED_TOKEN);
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(this.secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Date getExpireDate(Duration duration) {
        Instant expireAt = clock.instant().plus(duration);
        return Date.from(expireAt);
    }

}
