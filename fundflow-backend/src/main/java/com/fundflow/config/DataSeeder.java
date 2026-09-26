package com.fundflow.config;

import com.fundflow.entity.Campaign;
import com.fundflow.entity.CampaignStatus;
import com.fundflow.entity.Role;
import com.fundflow.entity.User;
import com.fundflow.repository.CampaignRepository;
import com.fundflow.repository.RoleRepository;
import com.fundflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
    private final CampaignRepository campaignRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app-admin.seed-email}")
    private String seedEmail;

    @Value("${app-admin.seed-password}")
    private String seedPassword;

    @Value("${app-admin.seed-full-name}")
    private String seedFullName;

    @Value("${app-demo.enabled:true}")
    private boolean demoDataEnabled;

    @Override
    public void run(String... args) {
        boolean adminExists = !userRepository.findByRole_Name("ROLE_ADMIN").isEmpty();
        if (adminExists) {
            seedDemoCampaigns();
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

        log.info(
                "Seeded default admin account: {} (change ADMIN_EMAIL/ADMIN_PASSWORD env vars for anything beyond local dev)",
                seedEmail);
        seedDemoCampaigns();
    }

    private void seedDemoCampaigns() {
        if (!demoDataEnabled || campaignRepository.countByStatus(CampaignStatus.APPROVED) > 0) {
            return;
        }

        Role organizerRole = roleRepository.findByName("ROLE_ORGANIZER")
                .orElseThrow(() -> new IllegalStateException("ROLE_ORGANIZER not found"));
        User demoOrganizer = userRepository.findByEmail("demo.organizer@fundflow.local")
                .orElseGet(() -> userRepository.save(User.builder()
                        .fullName("FundFlow Demo Organizer")
                        .email("demo.organizer@fundflow.local")
                        .passwordHash(passwordEncoder.encode("Demo@12345"))
                        .role(organizerRole)
                        .build()));

        LocalDate start = LocalDate.now().minusDays(12);
        campaignRepository.saveAll(List.of(
                demoCampaign("Assam Flood Relief", "Disaster Relief",
                        "Support local flood recovery with temporary shelter, clean water, and essential supplies. This demo campaign shows how FundFlow presents a reviewed community fundraiser.",
                        "Families in flood-affected communities",
                        "Local relief coordinators are distributing supplies through verified community partners.",
                        new BigDecimal("25000"), new BigDecimal("16400"), start, start.plusDays(48),
                        "/assets/images/campaign-disaster-relief.svg", demoOrganizer),
                demoCampaign("Learning Kits for Rural Students", "Education",
                        "Help provide notebooks, learning materials, and school supplies for students preparing for the next term. This is sample data for the FundFlow demo experience.",
                        "Students in rural schools",
                        "School coordinators will distribute materials during the upcoming term.",
                        new BigDecimal("12000"), new BigDecimal("7600"), start, start.plusDays(62),
                        "/assets/images/campaign-education.svg", demoOrganizer),
                demoCampaign("Community Health Support", "Medical",
                        "Help a neighborhood support group meet urgent care and transport needs for families who need assistance. This demo description uses only the information shown here.",
                        "Families seeking community health support",
                        "Funds are intended for documented care-related assistance and transport costs.",
                        new BigDecimal("18000"), new BigDecimal("11250"), start, start.plusDays(35),
                        "/assets/images/campaign-medical.svg", demoOrganizer),
                demoCampaign("Rebuild the Community Garden", "Community",
                        "Support volunteers restoring a shared community garden with tools, soil, and accessible planting beds for local residents.",
                        "Residents of the Riverside neighborhood",
                        "The volunteer group will coordinate purchases and publish progress updates.",
                        new BigDecimal("9000"), new BigDecimal("5400"), start, start.plusDays(74),
                        "/assets/images/campaign-community.svg", demoOrganizer)));
        log.info("Seeded approved FundFlow demo campaigns for local development");
    }

    private Campaign demoCampaign(String title, String category, String description, String beneficiaryName,
            String beneficiaryInfo, BigDecimal targetAmount, BigDecimal currentAmount,
            LocalDate startDate, LocalDate endDate, String imagePath, User organizer) {
        return Campaign.builder()
                .title(title)
                .description(description)
                .category(category)
                .targetAmount(targetAmount)
                .currentAmount(currentAmount)
                .startDate(startDate)
                .endDate(endDate)
                .organizer(organizer)
                .beneficiaryName(beneficiaryName)
                .beneficiaryInfo(beneficiaryInfo)
                .imagePath(imagePath)
                .status(CampaignStatus.APPROVED)
                .build();
    }
}
