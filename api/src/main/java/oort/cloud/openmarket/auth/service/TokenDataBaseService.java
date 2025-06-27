package oort.cloud.openmarket.auth.service;

import oort.cloud.openmarket.auth.data.AuthToken;
import oort.cloud.openmarket.auth.data.RefreshTokenPayload;
import oort.cloud.openmarket.auth.entity.RefreshToken;
import oort.cloud.openmarket.auth.repository.RefreshTokenRepository;
import oort.cloud.openmarket.auth.utils.jwt.JwtManager;
import oort.cloud.openmarket.auth.utils.jwt.JwtProperties;
import oort.cloud.openmarket.common.exception.auth.ExpiredTokenException;
import oort.cloud.openmarket.common.exception.auth.InvalidTokenException;
import oort.cloud.openmarket.common.exception.auth.UnauthorizedAccessException;
import oort.cloud.openmarket.common.exception.enums.ErrorType;
import oort.cloud.openmarket.user.data.UserDto;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class TokenDataBaseService implements TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties properties;
    private final JwtManager jwtManager;
    private final UserService userService;

    public TokenDataBaseService(RefreshTokenRepository refreshTokenRepository, JwtProperties properties, JwtManager jwtManager, UserService userService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.properties = properties;
        this.jwtManager = jwtManager;
        this.userService = userService;
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        Long userId = jwtManager.getRefreshTokenPayload(refreshToken).getUserId();
        RefreshToken savedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(UnauthorizedAccessException::new);

        if(!refreshToken.equals(savedToken.getToken()))
            throw new InvalidTokenException(ErrorType.INVALID_TOKEN);

        if(savedToken.getExpiredAt().isBefore(LocalDateTime.now()))
            throw new ExpiredTokenException(ErrorType.EXPIRED_TOKEN);

        Users user = userService.findUserById(savedToken.getUserId());
        return jwtManager.getAccessToken(
                user,
                Duration.ofMinutes(properties.accessTokenExpiredMinutes())
        );
    }

    @Override
    @Transactional
    public AuthToken createAuthToken(Users user) {
        Duration refreshDuration = Duration.ofDays(properties.refreshTokenExpiredDay());
        Duration accessDuration = Duration.ofMinutes(properties.accessTokenExpiredMinutes());

        String accessToken = jwtManager.getAccessToken(user, accessDuration);

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.getUserId())
                .orElseGet(() ->
                        RefreshToken.createRefreshToken(
                                user.getUserId(),
                                jwtManager.getRefreshToken(user, refreshDuration),
                                LocalDateTime.now().plus(refreshDuration)
                        )
                );
        refreshTokenRepository.save(refreshToken);

        return AuthToken.of(accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public void logout(String refreshToken){
        RefreshTokenPayload refreshTokenPayload = jwtManager.getRefreshTokenPayload(refreshToken);
        Long userid = refreshTokenPayload.getUserId();
        refreshTokenRepository.deleteByUserId(userid);
    }
}
