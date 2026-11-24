package edu.itc.shop;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.itc.shop.domain.UserAccount;
import edu.itc.shop.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ShopSecureApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private UserAccountRepository users;

	@Test
	void contextLoads() {
	}

	@Test
	void registersValidUserAndAuthenticatesWithCustomerRole() throws Exception {
		mockMvc.perform(post("/api/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"bob","password":"Correct-Horse-9"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.username").value("bob"))
				.andExpect(content().string(not(containsString("Correct-Horse-9"))));

		UserAccount bob = users.findByUsername("bob").orElseThrow();
		String passwordHash = jdbcTemplate.queryForObject(
				"select password_hash from users where username = ?", String.class, "bob");
		org.junit.jupiter.api.Assertions.assertTrue(passwordHash.startsWith("{bcrypt}$2a$10$"));
		org.junit.jupiter.api.Assertions.assertFalse(bob.toString().contains(passwordHash));

		mockMvc.perform(get("/api/me").with(httpBasic("bob", "Correct-Horse-9")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("bob"))
				.andExpect(jsonPath("$.roles[0]").value("ROLE_CUSTOMER"));
	}

	@Test
	void reportsPasswordPolicyValidationErrorsAsProblemJson() throws Exception {
		mockMvc.perform(post("/api/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"bob","password":"short"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Bad Request"))
				.andExpect(jsonPath("$.detail").value("Validation failed"))
				.andExpect(jsonPath("$.instance").value("/api/register"))
				.andExpect(jsonPath("$.errors[0].field").value("password"))
				.andExpect(jsonPath("$.errors[0].message").value("size must be between 12 and 72"))
				.andExpect(jsonPath("$.errors[1].field").value("password"))
				.andExpect(jsonPath("$.errors[1].message")
						.value("must contain an upper-case letter, a lower-case letter and a digit"));
	}

	@Test
	void rejectsDuplicateUsernamesWithConflict() throws Exception {
		mockMvc.perform(post("/api/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"alice","password":"Correct-Horse-9"}
								"""))
				.andExpect(status().isConflict());
	}

	@Test
	void allowsH2ConsolePathThroughSecurityChain() throws Exception {
		mockMvc.perform(get("/h2-console"))
				.andExpect(status().isNotFound())
				.andExpect(header().string("X-Frame-Options", "SAMEORIGIN"));
	}
}
