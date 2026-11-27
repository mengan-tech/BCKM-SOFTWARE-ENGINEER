package edu.itc.shop;

import edu.itc.shop.domain.Product;
import edu.itc.shop.domain.Role;
import edu.itc.shop.repository.ProductRepository;
import edu.itc.shop.service.AccountService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;

@SpringBootApplication
public class ShopSecureApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopSecureApplication.class, args);
	}

	@Bean
	@ConditionalOnBean(ProductRepository.class)
	CommandLineRunner seedProducts(ProductRepository repository) {
		return args -> {
			if (repository.count() == 0) {
				repository.saveAll(List.of(
						new Product("USB-C cable", new BigDecimal("7.90"), 25),
						new Product("Wireless mouse", new BigDecimal("19.90"), 12),
						new Product("Keyboard", new BigDecimal("49.50"), 8)));
			}
		};
	}

	@Bean
	@ConditionalOnBean(AccountService.class)
	CommandLineRunner seedAccounts(
			AccountService accounts,
			@Value("${shop.seed.admin-password:Dev-Admin-Password-9}") String adminPassword,
			@Value("${shop.seed.customer-password:Dev-Customer-Password-9}") String customerPassword) {
		return args -> {
			if (!accounts.existsByUsername("admin")) {
				accounts.register("admin", adminPassword, Set.of(Role.ADMIN, Role.CUSTOMER));
			}
			if (!accounts.existsByUsername("alice")) {
				accounts.register("alice", customerPassword, Set.of(Role.CUSTOMER));
			}
		};
	}

}
