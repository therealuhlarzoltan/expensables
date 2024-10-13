package hu.therealuhlarzoltan.expensables.cloud.authserver.services;

import hu.therealuhlarzoltan.expensables.cloud.authserver.exceptions.UserAlreadyExistsException;
import hu.therealuhlarzoltan.expensables.cloud.authserver.models.RoleEntity;
import hu.therealuhlarzoltan.expensables.cloud.authserver.models.UserEntity;
import hu.therealuhlarzoltan.expensables.cloud.authserver.repositories.RoleRepository;
import hu.therealuhlarzoltan.expensables.cloud.authserver.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Validated
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    @Value("${authentication.roles.default}")
    private String defaultRole;

    @Override
    public UserEntity registerUser(UserEntity userEntity) {
        if (userRepository.existsByUsername(userEntity.getUsername())) {
            throw new UserAlreadyExistsException("User with username " + userEntity.getUsername() + " already exists.");
        }

        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        RoleEntity userRole = roleRepository.findByName(defaultRole)
                .orElseThrow(() -> new RuntimeException("User Role not set."));
        userEntity.setRoles(new HashSet<>(Set.of(userRole)));
        return userRepository.save(userEntity);
    }

    @Override
    public Authentication authenticateUser(String username, String password) throws AuthenticationException {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
    }

    @Override
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
