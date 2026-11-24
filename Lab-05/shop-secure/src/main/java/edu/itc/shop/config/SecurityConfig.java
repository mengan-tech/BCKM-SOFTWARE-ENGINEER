package edu.itc.shop.config;

import tools.jackson.databind.ObjectMapper;
import edu.itc.shop.web.ApiExceptionHandler;
import java.io.IOException;
import java.net.URI;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain api(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        AuthenticationEntryPoint authenticationEntryPoint = (request, response, exception) ->
                writeProblem(objectMapper, request.getRequestURI(), response, HttpStatus.UNAUTHORIZED,
                        "Unauthorized", "Authentication required");
        AccessDeniedHandler accessDeniedHandler = (request, response, exception) ->
                writeProblem(objectMapper, request.getRequestURI(), response, HttpStatus.FORBIDDEN,
                        "Forbidden", "Access denied");

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                .formLogin(form -> form.disable())
                // CSRF is safe to disable because this API uses only Basic/Bearer credentials, not cookies.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }

    @Bean
    UserDetailsService temporaryUsers() {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password("{noop}admin").roles("ADMIN").build(),
                User.withUsername("alice").password("{noop}alice").roles("CUSTOMER").build());
    }

    private static void writeProblem(ObjectMapper objectMapper, String requestUri, HttpServletResponse response,
            HttpStatus status, String title, String detail) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        if (status == HttpStatus.UNAUTHORIZED) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Realm\"");
        }
        var problem = ApiExceptionHandler.problem(status, title, detail);
        problem.setInstance(URI.create(requestUri));
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
