package hu.therealuhlarzoltan.expensables.cloud.authserver.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;

public class AuthFilterConfig extends OAuth2AuthorizationServerConfiguration {
    @Override
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        return super.authorizationServerSecurityFilterChain(http);
    }
}
