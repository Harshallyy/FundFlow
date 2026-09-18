package com.fundflow.config;

import com.fundflow.entity.Role;
import com.fundflow.entity.User;
import com.fundflow.repository.RoleRepository;
import com.fundflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Runs once on every startup. There is intentionally no self-registration
 * path for admins (see AuthServiceImpl.register), so without this there
 * would be no way to ever create the first admin account. Idempotent -
 * does nothing once at least one ROLE_ADMIN user already exists.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app-admin.seed-email}")
    private String seedEmail;

    @Value("${app-admin.seed-password}")
    private String seedPassword;

    @Value("${app-admin.seed-full-name}")
    private String seedFullName;

    @Override
    public void run(String... args) {
        boolean adminExists = !userRepository.findByRole_Name("ROLE_ADMIN").isEmpty();
        if (adminExists) {
            return;
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException(
                        "ROLE_ADMIN not found - did you run db/schema.sql, which seeds the ROLE table?"));

        User admin = User.builder()
                .fullName(seedFullName)
                .email(seedEmail)
                .passwordHash(passwordEncoder.encode(seedPassword))
                .role(adminRole)
                .build();
        userRepository.save(admin);

        log.info("Seeded default admin account: {} (change ADMIN_EMAIL/ADMIN_PASSWORD env vars for anything beyond local dev)", seedEmail);
    }
}
