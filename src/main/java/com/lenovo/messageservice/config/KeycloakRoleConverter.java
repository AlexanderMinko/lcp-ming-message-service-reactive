package com.lenovo.messageservice.config;

import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

public class KeycloakRoleConverter implements Converter<Jwt, Flux<GrantedAuthority>> {

    @Override
    public Flux<GrantedAuthority> convert(Jwt jwt) {
        var realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
        return CollectionUtils.isEmpty(realmAccess)
            ? Flux.empty()
            : Flux.just(realmAccess.get("roles"))
            .map(role -> "ROLE_" + role)
            .map(SimpleGrantedAuthority::new);
    }
}
