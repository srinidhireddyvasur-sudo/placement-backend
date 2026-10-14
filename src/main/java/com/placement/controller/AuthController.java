package com.placement.controller;

import com.placement.model.AppUser;
import com.placement.model.Student;
import com.placement.repository.StudentRepository;
import com.placement.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final StudentRepository students;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository users, StudentRepository students, PasswordEncoder encoder) {
        this.users = users;
        this.students = students;
        this.encoder = encoder;
    }

    record RegisterReq(String username, String password, String name, String email, String phone,
                       String branch, Double cgpa, Integer backlogs, String skills) {}

    @PostMapping("/register")
    @Transactional
    public Map<String, String> register(@RequestBody RegisterReq r) {
        if (blank(r.username()) || blank(r.password()) || blank(r.name()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username, password and name are required");
        if (r.password().length() < 6)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        if (users.findByUsername(r.username().trim()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");

        AppUser u = new AppUser();
        u.username = r.username().trim();
        u.password = encoder.encode(r.password());
        u.role = "STUDENT";
        users.save(u);

        Student s = new Student();
        s.user = u;
        s.name = r.name().trim();
        s.email = r.email();
        s.phone = r.phone();
        s.branch = r.branch();
        s.cgpa = r.cgpa() == null ? 0 : r.cgpa();
        s.backlogs = r.backlogs() == null ? 0 : r.backlogs();
        s.skills = r.skills();
        students.save(s);
        return Map.of("message", "Registered successfully");
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication a) {
        if (a == null || a instanceof AnonymousAuthenticationToken || !a.isAuthenticated())
            return Map.of("authenticated", false);
        String role = a.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return Map.of("authenticated", true, "username", a.getName(), "role", role);
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
}
