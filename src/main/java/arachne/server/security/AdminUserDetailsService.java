package arachne.server.security;

import arachne.server.domain.User;
import arachne.server.repository.UserRepository;
import lombok.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Collections;

public class AdminUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    public AdminUserDetailsService(final @NonNull UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) {
        final User user = this.repo.findOneByUsername(username);
        if (null == user) {
            throw new UsernameNotFoundException("USER_NOT_FOUND");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

}
