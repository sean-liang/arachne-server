package arachne.server.security;

import arachne.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Order(Ordered.LOWEST_PRECEDENCE)
@Configuration
public class AdminWebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.requestMatchers().antMatchers("/admin/**").and().httpBasic().and()
                .authorizeRequests(authorize -> authorize.anyRequest().hasRole("ADMIN")).logout(logout -> {
            logout.logoutUrl("/admin/logout");
            logout.invalidateHttpSession(true);
        }).csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(new AdminUserDetailsService(userRepository)).passwordEncoder(this.adminUserPasswordEncoder());
    }

    @Bean
    public PasswordEncoder adminUserPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
