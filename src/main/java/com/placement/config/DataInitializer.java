package com.placement.config;

import com.placement.model.AppUser;
import com.placement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    @Value("${app.admin.username}") private String adminUser;
    @Value("${app.admin.password}") private String adminPass;

    public DataInitializer(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        AppUser u = users.findByUsername(adminUser).orElseGet(() -> {
            AppUser x = new AppUser();
            x.username = adminUser;
            x.password = "";
            return x;
        });
        if (u.password == null || u.password.isEmpty() || !encoder.matches(adminPass, u.password)) {
            u.password = encoder.encode(adminPass);
        }
        u.role = "ADMIN";
        users.save(u);
    }
}
