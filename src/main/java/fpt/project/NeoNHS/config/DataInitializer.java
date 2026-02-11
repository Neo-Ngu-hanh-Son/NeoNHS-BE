package fpt.project.NeoNHS.config;

import fpt.project.NeoNHS.entity.Attraction;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.enums.AttractionStatus;
import fpt.project.NeoNHS.entity.VendorProfile;
import fpt.project.NeoNHS.enums.UserRole;
import fpt.project.NeoNHS.repository.AttractionRepository;
import fpt.project.NeoNHS.repository.VendorProfileRepository;
import fpt.project.NeoNHS.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final VendorProfileRepository vendorProfileRepository;
    private final AttractionRepository attractionRepository;

    @Override
    public void run(String... args) {
        initializeAdminUser();
        initializeAttractions();
    }

    private void initializeAdminUser() {
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

    private void initializeAttractions() {
        if (attractionRepository.count() == 0) {
            List<Attraction> attractions = createNguHanhSonAttractions();
            attractionRepository.saveAll(attractions);
            log.info("Initialized {} attractions (Ngũ Hành Sơn - Five Mountains)", attractions.size());
        }
    }

    private List<Attraction> createNguHanhSonAttractions() {
        // Thủy Sơn (Water Mountain) - The largest and most famous
        Attraction thuySon = Attraction.builder()
                .name("Thủy Sơn (Water Mountain)")
                .description("""
                    Thủy Sơn is the largest and most famous of the five Marble Mountains. 
                    It features numerous caves and Buddhist temples, including the famous 
                    Huyen Khong Cave with its natural skylights and the Tam Thai Pagoda. 
                    The mountain represents the water element in Vietnamese cosmology and 
                    offers breathtaking views of Da Nang city and the East Sea from its summit.
                    
                    Key attractions include:
                    - Huyen Khong Cave (Mysterious Cave)
                    - Tam Thai Pagoda
                    - Linh Ung Pagoda
                    - Tang Chon Cave
                    - Vong Giang Dai (River Viewing Platform)
                    """)
                .address("Hòa Hải, Ngũ Hành Sơn, Đà Nẵng, Vietnam")
                .latitude(new BigDecimal("16.0034"))
                .longitude(new BigDecimal("108.2636"))
                .openHour(LocalTime.of(7, 0))
                .closeHour(LocalTime.of(17, 30))
                .status(AttractionStatus.OPEN.name())
                .thumbnailUrl("https://images.unsplash.com/photo-1559592413-7cec4d0cae2b?w=800")
                .isActive(true)
                .build();

        // Mộc Sơn (Wood Mountain)
        Attraction mocSon = Attraction.builder()
                .name("Mộc Sơn (Wood Mountain)")
                .description("""
                    Mộc Sơn represents the wood element and is characterized by lush vegetation 
                    covering its slopes. Though smaller than Thủy Sơn, it offers a peaceful 
                    retreat with beautiful greenery and traditional architecture.
                    
                    The mountain is known for its serene atmosphere and is less crowded 
                    than its larger neighbor, making it perfect for those seeking tranquility.
                    """)
                .address("Hòa Hải, Ngũ Hành Sơn, Đà Nẵng, Vietnam")
                .latitude(new BigDecimal("16.0045"))
                .longitude(new BigDecimal("108.2628"))
                .openHour(LocalTime.of(7, 0))
                .closeHour(LocalTime.of(17, 30))
                .status(AttractionStatus.OPEN.name())
                .thumbnailUrl("https://images.unsplash.com/photo-1528127269322-539801943592?w=800")
                .isActive(true)
                .build();

        // Hỏa Sơn (Fire Mountain)
        Attraction hoaSon = Attraction.builder()
                .name("Hỏa Sơn (Fire Mountain)")
                .description("""
                    Hỏa Sơn represents the fire element in Vietnamese cosmology. According to 
                    local legend, this mountain was formed from the flames of a dragon. 
                    
                    The mountain consists of two peaks - Dương Hỏa Sơn (Positive Fire) and 
                    Âm Hỏa Sơn (Negative Fire). It features unique rock formations and 
                    offers a different perspective of the Marble Mountains complex.
                    """)
                .address("Hòa Hải, Ngũ Hành Sơn, Đà Nẵng, Vietnam")
                .latitude(new BigDecimal("16.0028"))
                .longitude(new BigDecimal("108.2620"))
                .openHour(LocalTime.of(7, 0))
                .closeHour(LocalTime.of(17, 30))
                .status(AttractionStatus.OPEN.name())
                .thumbnailUrl("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800")
                .isActive(true)
                .build();

        // Kim Sơn (Metal Mountain)
        Attraction kimSon = Attraction.builder()
                .name("Kim Sơn (Metal Mountain)")
                .description("""
                    Kim Sơn represents the metal element and is known for its distinctive 
                    rock formations that glitter in the sunlight, reminiscent of precious metals.
                    
                    The mountain has a rich history dating back centuries and features 
                    traditional stone-cutting workshops at its base, where local artisans 
                    continue the centuries-old tradition of marble sculpture.
                    """)
                .address("Hòa Hải, Ngũ Hành Sơn, Đà Nẵng, Vietnam")
                .latitude(new BigDecimal("16.0020"))
                .longitude(new BigDecimal("108.2642"))
                .openHour(LocalTime.of(7, 0))
                .closeHour(LocalTime.of(17, 30))
                .status(AttractionStatus.OPEN.name())
                .thumbnailUrl("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800")
                .isActive(true)
                .build();

        // Thổ Sơn (Earth Mountain)
        Attraction thoSon = Attraction.builder()
                .name("Thổ Sơn (Earth Mountain)")
                .description("""
                    Thổ Sơn represents the earth element and is characterized by its 
                    reddish-brown soil and sturdy rock formations. It symbolizes stability 
                    and grounding in Vietnamese philosophy.
                    
                    The mountain offers panoramic views of the surrounding area and is 
                    home to several small shrines and meditation spots used by locals 
                    for centuries.
                    """)
                .address("Hòa Hải, Ngũ Hành Sơn, Đà Nẵng, Vietnam")
                .latitude(new BigDecimal("16.0015"))
                .longitude(new BigDecimal("108.2650"))
                .openHour(LocalTime.of(7, 0))
                .closeHour(LocalTime.of(17, 30))
                .status(AttractionStatus.CLOSED.name())
                .thumbnailUrl("https://images.unsplash.com/photo-1454496522488-7a8e488e8606?w=800")
                .isActive(false)
                .build();

        return Arrays.asList(thuySon, mocSon, hoaSon, kimSon, thoSon);
    }
}

