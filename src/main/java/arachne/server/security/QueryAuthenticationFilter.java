package arachne.server.security;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@AllArgsConstructor
public class QueryAuthenticationFilter extends OncePerRequestFilter {

    private final String clientIdField;

    private final String tokenField;

    public QueryAuthenticationFilter() {
        this.clientIdField = "clientId";
        this.tokenField = "token";
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() != null && context.getAuthentication().isAuthenticated()) {
            // do nothing
        } else {
            final Map<String, String[]> params = request.getParameterMap();
            final String[] clientId = params.containsKey(this.clientIdField) ? params.get(this.clientIdField)
                    : new String[0];
            final String[] token = params.containsKey(this.tokenField) ? params.get(this.tokenField) : new String[0];

            if (clientId.length > 0 && token.length > 0) {
                final Authentication auth = new UsernamePasswordAuthenticationToken(clientId[0], token[0]);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

//			request.setAttribute("arachne.server.security.QueryAuthenticationFilter.FILTERED", true);
        }

        filterChain.doFilter(request, response);
    }

}
