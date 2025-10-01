package com.deharri.ums.config.security.user;

import com.deharri.ums.user.entity.CoreUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

    private final CoreUser coreUser;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return coreUser.getUserData().getUserRoles().stream()
                .map(m -> m.toString().substring(5))
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return coreUser.getPassword();
    }

    @Override
    public String getUsername() {
        return coreUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}