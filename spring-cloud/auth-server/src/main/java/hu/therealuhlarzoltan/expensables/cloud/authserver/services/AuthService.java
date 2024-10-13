package hu.therealuhlarzoltan.expensables.cloud.authserver.services;

import hu.therealuhlarzoltan.expensables.cloud.authserver.models.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public interface AuthService {
    UserEntity registerUser(UserEntity userEntity);
    Authentication authenticateUser(String username, String password) throws AuthenticationException;
    UserEntity getUserByUsername(String username);
}
