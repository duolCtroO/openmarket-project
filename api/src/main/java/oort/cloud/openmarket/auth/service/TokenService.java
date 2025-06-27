package oort.cloud.openmarket.auth.service;

import oort.cloud.openmarket.auth.data.AuthToken;
import oort.cloud.openmarket.user.data.UserDto;
import oort.cloud.openmarket.user.entity.Users;

public interface TokenService {
    String refreshAccessToken(String refreshToken);

    AuthToken createAuthToken(Users user);

    void logout(String refreshToken);
}
