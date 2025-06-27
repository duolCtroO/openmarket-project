package oort.cloud.openmarket.auth.component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import oort.cloud.openmarket.auth.data.AccessTokenPayload;
import oort.cloud.openmarket.auth.data.AuthToken;
import oort.cloud.openmarket.auth.utils.jwt.JwtManager;
import oort.cloud.openmarket.auth.utils.jwt.JwtProperties;
import oort.cloud.openmarket.common.exception.auth.InvalidTokenException;
import oort.cloud.openmarket.common.exception.enums.ErrorType;
import oort.cloud.openmarket.user.UserFixture;
import oort.cloud.openmarket.user.data.UserDto;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.enums.UserRole;
import oort.cloud.openmarket.user.enums.UserStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
class JwtManagerTest {
    @Mock
    private JwtProperties jwtProperties;
    private JwtManager jwtManager;
    private Clock clock;
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 테스트용 키
    @BeforeEach
    void setup() {
        when(jwtProperties.getSecretKey()).thenReturn(secretKey);
        when(jwtProperties.accessTokenExpiredMinutes()).thenReturn(30); // 30분
        when(jwtProperties.refreshTokenExpiredDay()).thenReturn(7); // 7일
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        jwtManager = new JwtManager(jwtProperties, clock);
    }

    @Test
    @DisplayName("JWT Access 토큰 생성이 성공한다.")
    void success_create_jwt(){
        //given
        Users user = UserFixture.getUser();

        //when
        AuthToken authToken = jwtManager.createAuthToken(user);
        String accessToken = authToken.getAccessToken();
        AccessTokenPayload parseUserPayload = jwtManager.getAccessTokenPayload(accessToken);

        //then
        assertEquals(parseUserPayload.getUserId(), user.getUserId());
    }

    @Test
    @DisplayName("JWT 토큰 검증이 실패할 경우 InvalidTokenException 타입의 예외를 던진다.")
    void fail_validate_token(){
        //given
        Users user = UserFixture.getUser();

        //when
        AuthToken authToken = jwtManager.createAuthToken(user);
        String accessToken = authToken.getAccessToken();
        String diffToken = accessToken.substring(0, 3) + "X" + accessToken.substring(4);

        //then
        String message =
                Assertions.assertThrows(InvalidTokenException.class, () -> jwtManager.getAccessTokenPayload(diffToken)).getMessage();
        assertEquals(message, ErrorType.INVALID_TOKEN.getMessage());
    }


    @Test
    @DisplayName("Access Token의 만료 시간이 정확히 30분 뒤인지 확인")
    void accessToken_expiration_is_valid() {
        Users user = UserFixture.getUser();
        String accessToken = jwtManager.createAuthToken(user).getAccessToken();

        Claims claims = extractClaims(accessToken);
        Date expiration = claims.getExpiration();

        Instant expectedExpiration = this.clock.instant()
                .plus(30, ChronoUnit.MINUTES)
                .truncatedTo(ChronoUnit.SECONDS);

        assertEquals(expectedExpiration, expiration.toInstant());
    }


    @Test
    @DisplayName("Refresh Token의 만료시간이 정확히 7일 이후")
    void refreshToken_has_longer_expiration() {
        Users user = UserFixture.getUser();
        AuthToken token = jwtManager.createAuthToken(user);

        Claims refreshClaims = extractClaims(token.getRefreshToken());
        Date expiration = refreshClaims.getExpiration();

        Instant expectedExpiration = this.clock.instant()
                .plus(7, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.SECONDS);

        assertEquals(expectedExpiration, expiration.toInstant());
    }

    @Test
    @DisplayName("Access Token의 User 정보 파싱이 성공한다.")
    void access_token_userInfo() {
        Users user = UserFixture.getUser();
        AuthToken token = jwtManager.createAuthToken(user);
        AccessTokenPayload extractUser = jwtManager.getAccessTokenPayload(token.getAccessToken());

        assertEquals(user.getUserId(), extractUser.getUserId());
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}