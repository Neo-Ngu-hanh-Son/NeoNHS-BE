package fpt.project.NeoNHS.config;

import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.entity.VendorProfile;
import fpt.project.NeoNHS.enums.UserRole;
import fpt.project.NeoNHS.repository.VendorProfileRepository;
import fpt.project.NeoNHS.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final VendorProfileRepository vendorProfileRepository;

    @Override
    public void run(String... args) {
        if (userService.countUsers() == 0) {
            // Create Admin user
            User admin = User.builder()
                    .fullname("Admin")
                    .email("admin@neonhs.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .isVerified(true)
                    .isBanned(false)
                    .build();

            userService.createUser(admin);
            log.info("Admin user created: admin@neonhs.com / admin123");

            // Create Vendor user with VendorProfile
            User vendorUser = User.builder()
                    .fullname("Test Vendor")
                    .email("vendor@neonhs.com")
                    .passwordHash(passwordEncoder.encode("vendor123"))
                    .role(UserRole.VENDOR)
                    .isActive(true)
                    .isVerified(true)
                    .isBanned(false)
                    .build();

            User savedVendorUser = userService.createUser(vendorUser);

            VendorProfile vendorProfile = VendorProfile.builder()
                    .user(savedVendorUser)
                    .businessName("NeoNHS Workshop Center")
                    .description("Premium traditional craft workshops in Ngu Hanh Son")
                    .address("123 Marble Mountain Road, Da Nang, Vietnam")
                    .latitude("16.0020")
                    .longitude("108.2633")
                    .taxCode("0123456789")
                    .bankName("Vietcombank")
                    .bankAccountNumber("1234567890123")
                    .bankAccountName("NGUYEN VAN A")
                    .isVerified(true)
                    .build();

            vendorProfileRepository.save(vendorProfile);
            log.info("Vendor user created: vendor@neonhs.com / vendor123");
            log.info("Vendor profile created: NeoNHS Workshop Center (verified)");
        }
    }
}
