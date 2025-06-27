package oort.cloud.openmarket.auth.service;

import oort.cloud.openmarket.auth.controller.request.LoginRequest;
import oort.cloud.openmarket.auth.controller.request.SignUpRequest;
import oort.cloud.openmarket.auth.data.AuthToken;
import oort.cloud.openmarket.auth.utils.jwt.JwtManager;
import oort.cloud.openmarket.data.LoginRequestTest;
import oort.cloud.openmarket.data.SignUpRequestTest;
import oort.cloud.openmarket.common.exception.business.BusinessException;
import oort.cloud.openmarket.common.exception.business.DuplicateEmailException;
import oort.cloud.openmarket.common.exception.enums.ErrorType;
import oort.cloud.openmarket.user.data.UserDto;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.enums.UserRole;
import oort.cloud.openmarket.user.enums.UserStatus;
import oort.cloud.openmarket.user.repository.UserRepository;
import oort.cloud.openmarket.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService authService;
    private UserRepository userRepository;
    private BCryptPasswordEncoder encoder;
    private UserService userService;
    private JwtManager jwtManager;

    @BeforeEach
    void init(){
        encoder = mock(BCryptPasswordEncoder.class);
        userService = mock(UserService.class);
        jwtManager = mock(JwtManager.class);
        userRepository = mock(UserRepository.class);
        authService = new AuthService(userService, encoder);
    }

    @Test
    @DisplayName("회원가입이 성공한다.")
    void success_sign_up() {
        // given
        SignUpRequest request = mock(SignUpRequest.class);

        UserDto expectedDto = UserDto.of(1L, "test@email.com",
                "tester", "01012345678", UserRole.BUYER, UserStatus.ACTIVE);

        when(userService.save(request)).thenReturn(expectedDto);

        // when
        UserDto result = authService.signUp(request);

        // then
        assertEquals(expectedDto, result);
        verify(userService).save(request);
    }


    @Test
    @DisplayName("이메일이 중복되면 예외가 발생한다")
    void duplicate_email() {
        SignUpRequest request = new SignUpRequestTest(
                "test@email.com", "1234", "test", "12312341234", UserRole.BUYER);

        when(userService.save(request)).thenThrow(new DuplicateEmailException());

        // when & then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorType.DUPLICATE_EMAIL.getMessage());

        verify(userRepository, never()).save(any(Users.class));
    }

    @Test
    @DisplayName("정상 로그인 시 UserDto 타입 객체를 반환한다.")
    void loginSuccess() {
        //given
        LoginRequest request = getLoginRequest();
        AuthToken mockToken = AuthToken.of("accessToken", "refreshToken");
        Users user = Users.createUser("test@example.com", "1234", "test", "2134", UserRole.BUYER);

        //when
        when(userService.findUserByEmail(request.getEmail())).thenReturn(user);
        when(encoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtManager.createAuthToken(any(Users.class))).thenReturn(mockToken);

        Users loginUser = authService.login(request);

        //then
        assertEquals(user.getUserId(), loginUser.getUserId());
    }

    private LoginRequest getLoginRequest() {
        return new LoginRequestTest("test@email.com", "1234");
    }

}