package arachne.server.security;

import org.springframework.security.crypto.password.PasswordEncoder;

public class PlaintextPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(final CharSequence rawPassword) {
        return rawPassword.toString();
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        return rawPassword != null && rawPassword.toString().equals(encodedPassword);
    }

}
