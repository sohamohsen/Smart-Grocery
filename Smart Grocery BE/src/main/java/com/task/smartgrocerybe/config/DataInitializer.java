package com.task.smartgrocerybe.config;

import com.task.smartgrocerybe.model.Category;
import com.task.smartgrocerybe.model.Product;
import com.task.smartgrocerybe.model.User;
import com.task.smartgrocerybe.model.enums.Role;
import com.task.smartgrocerybe.repository.CategoryRepository;
import com.task.smartgrocerybe.repository.ProductRepository;
import com.task.smartgrocerybe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedApplicationData() {
        return args -> {
            seedUsers();
            seedDemoCatalog();
        };
    }

    private void seedUsers() {
        createUserIfMissing(
                "superadmin",
                "superadmin@smartgrocery.local",
                "Super Admin",
                Role.SUPER_ADMIN
        );
        createUserIfMissing(
                "admin",
                "admin@smartgrocery.local",
                "Admin User",
                Role.ADMIN
        );
        createUserIfMissing(
                "user",
                "user@smartgrocery.local",
                "Demo User",
                Role.USER
        );
    }

    private void createUserIfMissing(
            String username,
            String email,
            String name,
            Role role) {

        if (userRepository.existsByUsername(username)) {
            return;
        }

        userRepository.save(User.builder()
                .username(username)
                .email(email)
                .name(name)
                .password(passwordEncoder.encode("123456"))
                .role(role)
                .isActive(true)
                .build());

        log.info("Seeded default {} account with username '{}'", role, username);
    }

    private void seedDemoCatalog() {
        Category pantry = findOrCreateCategory(
                "Pantry Staples",
                "Shelf-ready basics for a quick product demo."
        );
        findOrCreateCategory(
                "Fresh Produce",
                "Fruit and vegetable products."
        );
        findOrCreateCategory(
                "Dairy & Eggs",
                "Milk, yogurt, eggs, and related items."
        );

        if (productRepository.count() > 0 || pantry == null) {
            return;
        }

        productRepository.save(Product.builder()
                .name("Whole Wheat Pasta")
                .description("Demo product seeded for reviewer sign-in flows.")
                .brand("Smart Grocery")
                .price(new BigDecimal("3.49"))
                .barcode("DEMO-PASTA-001")
                .imageUrl("")
                .categoryId(pantry.getId())
                .isApproved(true)
                .isDeleted(false)
                .build());

        log.info("Seeded demo catalog product for reviewer walkthroughs");
    }

    private Category findOrCreateCategory(String name, String description) {
        return categoryRepository.findAll().stream()
                .filter(category -> name.equalsIgnoreCase(category.getName()))
                .findFirst()
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .description(description)
                        .isDeleted(false)
                        .build()));
    }
}
