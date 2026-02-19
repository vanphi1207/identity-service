package me.ihqqq.identity_service.configuration;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.identity_service.entity.User;
import me.ihqqq.identity_service.enums.Role;
import me.ihqqq.identity_service.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    private PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository) {

        return args -> {
            if(userRepository.findByUsername("admin").isEmpty()) {
                var roles = new HashSet<String>();
                roles.add(Role.ADMIN.name());

                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("Default admin user has been created with default password: admin, please change it");
            }
        };
    }
}
