package com.placement.config;

import com.placement.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository repo) {
        return username -> repo.findByUsername(username)
                .map(u -> org.springframework.security.core.userdetails.User
                        .withUsername(u.username).password(u.password).roles(u.role).build())
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
            .authorizeHttpRequests(a -> a
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/favicon.ico", "/error", "/login", "/api/auth/**").permitAll()
                .requestMatchers("/admin.html", "/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/student.html", "/api/student/**").hasRole("STUDENT")
                .anyRequest().authenticated())
            .formLogin(f -> f
                .loginPage("/index.html")
                .loginProcessingUrl("/login")
                .successHandler((req, res, auth) -> {
                    String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
                    res.setStatus(200);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"role\":\"" + role + "\"}");
                })
                .failureHandler((req, res, ex) -> res.sendError(401, "Invalid username or password"))
                .permitAll())
            .logout(l -> l.logoutUrl("/logout").logoutSuccessHandler((req, res, auth) -> res.setStatus(200)))
            .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
                if (req.getRequestURI().startsWith("/api")) res.sendError(401);
                else res.sendRedirect("/index.html");
            }));
        return http.build();
    }
}
