package com.deharri.ums.permission;

import com.deharri.ums.user.UserRepository;
import com.deharri.ums.user.entity.CoreUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionService {

    private final UserRepository userRepository;

    public String getLoggedInUsersUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString(); // usually the username as string
        }
    }

    public CoreUser getLoggedInUser() {
        String username = getLoggedInUsersUsername();
        CoreUser coreUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return coreUser;
    }

}
