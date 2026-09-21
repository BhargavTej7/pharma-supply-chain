package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	ApplicationRunner bootstrapAdmin(UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			@Value("${bootstrap.admin.email:}") String adminEmail,
			@Value("${bootstrap.admin.password:}") String adminPassword) {
		return args -> {
			if (adminEmail.isBlank() && adminPassword.isBlank()) {
				return;
			}
			if (adminEmail.isBlank() || adminPassword.isBlank()) {
				throw new IllegalStateException("Both bootstrap admin credentials must be configured together");
			}
			userRepository.findByEmail(adminEmail).ifPresentOrElse(existing -> {
				if (!"ADMIN".equals(existing.getRole())) {
					throw new IllegalStateException("Bootstrap admin email belongs to a non-admin user");
				}
			}, () -> {
				User admin = new User("Platform Administrator", adminEmail,
						passwordEncoder.encode(adminPassword), "ADMIN");
				userRepository.save(admin);
			});
		};
	}

}
