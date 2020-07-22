package arachne.server.security;

import arachne.server.domain.Worker;
import arachne.server.service.WorkerService;
import lombok.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Collections;

public class WorkerUserDetailsService implements UserDetailsService {

    private final WorkerService service;

    public WorkerUserDetailsService(final @NonNull WorkerService service) {
        this.service = service;
    }

    @Override
    public UserDetails loadUserByUsername(final String clientId) {
        final Worker worker = this.service.getById(clientId)
                .orElseThrow(() -> new UsernameNotFoundException("WORKER_NOT_FOUND"));
        return new org.springframework.security.core.userdetails.User(worker.getId(), worker.getToken(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_WORKER")));
    }

}
