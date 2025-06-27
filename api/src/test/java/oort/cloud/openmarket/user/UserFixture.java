package oort.cloud.openmarket.user;

import oort.cloud.openmarket.user.entity.Users;
import oort.cloud.openmarket.user.enums.UserRole;

public class UserFixture {
    public static Users getUser(){
        return Users.createUser("test@email.com", "test",
                "user","12312341234", UserRole.BUYER);
    }
}
