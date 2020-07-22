package arachne.server.security;

import arachne.server.service.WorkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
public class WorkerWebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private WorkerService service;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.requestMatchers().antMatchers("/worker/**").and().httpBasic().and()
                .addFilterAfter(new QueryAuthenticationFilter(), BasicAuthenticationFilter.class).sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().headers().cacheControl().disable().and()
                .authorizeRequests(authorize -> authorize.anyRequest().hasRole("WORKER")).csrf().disable();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(new WorkerUserDetailsService(service)).passwordEncoder(new PlaintextPasswordEncoder());
    }

}
