package oort.cloud.openmarket.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import oort.cloud.openmarket.auth.controller.request.LoginRequest;
import oort.cloud.openmarket.auth.controller.request.SignUpRequest;
import oort.cloud.openmarket.auth.data.AuthToken;
import oort.cloud.openmarket.auth.service.AuthService;
import oort.cloud.openmarket.auth.service.TokenDataBaseService;
import oort.cloud.openmarket.auth.service.TokenRedisService;
import oort.cloud.openmarket.auth.service.TokenService;
import oort.cloud.openmarket.auth.utils.TokenCookieHelper;
import oort.cloud.openmarket.user.data.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthService authService;
//    private final TokenService tokenService; 성능 테스트를 위해 구현 클래스 직접 주입
    private final TokenService tokenDataBaseService;
    private final TokenService tokenRedisService;
    private final TokenCookieHelper tokenCookieHelper;
    public AuthController(AuthService authService,
                          TokenDataBaseService tokenDataBaseService,
                          TokenRedisService tokenRedisService,
                          TokenCookieHelper tokenCookieHelper) {
        this.authService = authService;
        this.tokenDataBaseService = tokenDataBaseService;
        this.tokenRedisService = tokenRedisService;
        this.tokenCookieHelper = tokenCookieHelper;
    }

    @PostMapping("/v1/auth/sign-up")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignUpRequest signUprequest){
        authService.signUp(signUprequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/v1/auth/login")
    public ResponseEntity<AuthToken> loginByDatabase(@RequestBody @Valid LoginRequest loginRequest
            , HttpServletResponse response){
        UserDto user = authService.login(loginRequest);
        AuthToken authToken = tokenDataBaseService.createAuthToken(user);
        tokenCookieHelper.addRefreshTokenCookie(response, authToken.getRefreshToken());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authToken);
    }

    @PostMapping("/v1/auth/logout")
    public ResponseEntity<Void> logoutByDatabase(HttpServletRequest request,
                                       HttpServletResponse response){
        String refreshToken = tokenCookieHelper.extractRefreshTokenFromCookies(request.getCookies());
        tokenCookieHelper.removeRefreshTokenCookie(response);
        tokenDataBaseService.logout(refreshToken);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @PostMapping("/v1/auth/refresh-token")
    public ResponseEntity<AuthToken> refreshTokenByDatabase(HttpServletRequest request){
        String refreshToken = tokenCookieHelper.extractRefreshTokenFromCookies(request.getCookies());
        String accessToken = tokenDataBaseService.refreshAccessToken(refreshToken);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(AuthToken.of(accessToken, refreshToken));
    }


    @PostMapping("/v2/auth/login")
    public ResponseEntity<AuthToken> loginByRedis(@RequestBody @Valid LoginRequest loginRequest
    , HttpServletResponse response){
        UserDto user = authService.login(loginRequest);
        AuthToken authToken = tokenRedisService.createAuthToken(user);
        tokenCookieHelper.addRefreshTokenCookie(response, authToken.getRefreshToken());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authToken);
    }

    @PostMapping("/v2/auth/logout")
    public ResponseEntity<Void> logoutByRedis(HttpServletRequest request,
                                                   HttpServletResponse response){
        String refreshToken = tokenCookieHelper.extractRefreshTokenFromCookies(request.getCookies());
        tokenCookieHelper.removeRefreshTokenCookie(response);
        tokenRedisService.logout(refreshToken);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @PostMapping("/v2/auth/refresh-token")
    public ResponseEntity<AuthToken> refreshTokenByRedis(HttpServletRequest request){
        String refreshToken = tokenCookieHelper.extractRefreshTokenFromCookies(request.getCookies());
        String accessToken = tokenRedisService.refreshAccessToken(refreshToken);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(AuthToken.of(accessToken, refreshToken));
    }


}
