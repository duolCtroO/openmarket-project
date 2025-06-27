package oort.cloud.openmarket.auth.service;

import oort.cloud.openmarket.auth.data.AuthToken;
import oort.cloud.openmarket.auth.data.RefreshTokenPayload;
import oort.cloud.openmarket.auth.repository.RefreshTokenRedisRepository;
import oort.cloud.openmarket.auth.utils.jwt.JwtManager;
import oort.cloud.openmarket.auth.utils.jwt.JwtProperties;
import oort.cloud.openmarket.common.exception.auth.InvalidTokenException;
import oort.cloud.openmarket.common.exception.auth.UnauthorizedAccessException;
import oort.cloud.openmarket.common.exception.enums.ErrorType;
import oort.cloud.openmarket.user.data.UserDto;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.service.UserService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Primary
@Service
public class TokenRedisService implements TokenService{
    private final JwtProperties properties;
    private final JwtManager jwtManager;
    private final UserService userService;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public TokenRedisService(JwtProperties properties, JwtManager jwtManager, UserService userService, RefreshTokenRedisRepository refreshTokenRedisRepository) {
        this.properties = properties;
        this.jwtManager = jwtManager;
        this.userService = userService;
        this.refreshTokenRedisRepository = refreshTokenRedisRepository;
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        Long userId = jwtManager.getRefreshTokenPayload(refreshToken).getUserId();

        String savedToken = refreshTokenRedisRepository.read(userId)
                .orElseThrow(UnauthorizedAccessException::new);

        if(!savedToken.equals(refreshToken))
            throw new InvalidTokenException(ErrorType.INVALID_TOKEN);

        Users user = userService.findUserById(userId);
        return jwtManager.getAccessToken(
                user,
                Duration.ofMinutes(properties.accessTokenExpiredMinutes())
        );
    }

    @Override
    public AuthToken createAuthToken(Users user) {
        Duration refreshDuration = Duration.ofDays(properties.refreshTokenExpiredDay());
        Duration accessDuration = Duration.ofMinutes(properties.accessTokenExpiredMinutes());

        String accessToken = jwtManager.getAccessToken(user, accessDuration);
        String refreshToken = getOrCreateRefreshToken(user, refreshDuration);

        refreshTokenRedisRepository.createOrUpdate(user.getUserId(), refreshToken, refreshDuration);

        return AuthToken.of(accessToken, refreshToken);
    }

    private String getOrCreateRefreshToken(Users user, Duration refreshDuration) {
        return refreshTokenRedisRepository.read(user.getUserId())
                .orElseGet(() -> jwtManager.getRefreshToken(user, refreshDuration));
    }

    @Override
    public void logout(String refreshToken) {
        RefreshTokenPayload refreshTokenPayload = jwtManager.getRefreshTokenPayload(refreshToken);
        Long userid = refreshTokenPayload.getUserId();
        refreshTokenRedisRepository.remove(userid);
    }
}
