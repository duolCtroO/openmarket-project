package oort.cloud.openmarket.auth.service;

import oort.cloud.openmarket.auth.controller.request.LoginRequest;
import oort.cloud.openmarket.auth.controller.request.SignUpRequest;
import oort.cloud.openmarket.common.exception.business.InvalidPasswordException;
import oort.cloud.openmarket.common.exception.business.NotFoundResourceException;
import oort.cloud.openmarket.user.data.UserDto;
import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto signUp(SignUpRequest request){
        return userService.save(request);
    }

    public Users login(LoginRequest loginRequest){
        Users user = userService.findUserByEmail(loginRequest.getEmail());
        validateUser(loginRequest, user);
        return user;
    }

    private void validateUser(LoginRequest loginRequest, Users user) {
        if(user == null)
            throw new NotFoundResourceException("조회된 유저 정보가 없습니다.");
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
            throw new InvalidPasswordException();
    }




}
