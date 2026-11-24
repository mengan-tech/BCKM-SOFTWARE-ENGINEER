package edu.itc.shop;

import edu.itc.shop.domain.Product;
import edu.itc.shop.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ShopSecureApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopSecureApplication.class, args);
	}

	@Bean
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

}
