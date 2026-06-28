package com.urlshortener.controller;

import com.urlshortener.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@Transactional
class UrlControllerTest {

	@DynamicPropertySource
	static void registerPostgres(DynamicPropertyRegistry registry) {
		IntegrationTestSupport.registerPostgresProperties(registry);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private Clock clock;

	@Autowired
	private com.urlshortener.repository.ShortUrlRepository shortUrlRepository;

	@Test
	void createUrlWithReservedCustomAliasReturns400() throws Exception {
		mockMvc.perform(post("/api/v1/urls")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"originalUrl":"https://example.com","customAlias":"api"}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("INVALID_ALIAS"));
	}

	@Test
	void createUrlWithTooShortCustomAliasReturns400() throws Exception {
		mockMvc.perform(post("/api/v1/urls")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"originalUrl":"https://example.com","customAlias":"ab"}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("INVALID_ALIAS"));
	}

	@Test
	void createUrlWithValidCustomAliasReturns201() throws Exception {
		mockMvc.perform(post("/api/v1/urls")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"originalUrl":"https://example.com/my-page","customAlias":"my-custom-link"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.alias").value("my-custom-link"))
				.andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/my-custom-link"));
	}

	@Test
	void createUrlWithDuplicateCustomAliasReturns409() throws Exception {
		String body = """
				{"originalUrl":"https://example.com/page","customAlias":"dup-alias-test"}
				""";

		mockMvc.perform(post("/api/v1/urls").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/urls").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("ALIAS_CONFLICT"));
	}

	@Test
	void createUrlWithInvalidUrlReturns400() throws Exception {
		mockMvc.perform(post("/api/v1/urls")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"originalUrl":"not-a-valid-url"}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("INVALID_URL"));
	}

	@Test
	void createUrlWithPastExpiryReturns400() throws Exception {
		mockMvc.perform(post("/api/v1/urls")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"originalUrl":"https://example.com","expiresAt":"2020-01-01T00:00:00Z"}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("INVALID_EXPIRY"));
	}

	@Test
	void createUrlWithCustomExpiryUsesProvidedDate() throws Exception {
		mockMvc.perform(post("/api/v1/urls")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"originalUrl":"https://example.com/custom-expiry","expiresAt":"2030-06-28T12:00:00Z"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.expiresAt").value("2030-06-28T12:00:00Z"));
	}

	@Test
	void createUrlWithoutExpiryUsesDefaultOneMinute() throws Exception {
		mockMvc.perform(post("/api/v1/urls")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"originalUrl":"https://example.com/some/path"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.alias").value(matchesPattern("[a-zA-Z0-9]{7}")))
				.andExpect(jsonPath("$.originalUrl").value("https://example.com/some/path"))
				.andExpect(jsonPath("$.shortUrl").value(matchesPattern("http://localhost:8080/[a-zA-Z0-9]{7}")))
				.andExpect(jsonPath("$.accessCount").value(0))
				.andExpect(jsonPath("$.createdAt").exists())
				.andExpect(jsonPath("$.expiresAt").exists());
	}

	@Test
	void getMetadataForValidAliasReturns200() throws Exception {
		String alias = "meta-" + UUID.randomUUID().toString().substring(0, 8);

		mockMvc.perform(post("/api/v1/urls")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"originalUrl":"https://example.com/metadata","customAlias":"%s","expiresAt":"2030-01-01T00:00:00Z"}
						""".formatted(alias)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/urls/" + alias))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.alias").value(alias))
				.andExpect(jsonPath("$.originalUrl").value("https://example.com/metadata"))
				.andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/" + alias))
				.andExpect(jsonPath("$.accessCount").value(0))
				.andExpect(jsonPath("$.createdAt").exists())
				.andExpect(jsonPath("$.expiresAt").value("2030-01-01T00:00:00Z"));
	}

	@Test
	void getMetadataForUnknownAliasReturns404() throws Exception {
		mockMvc.perform(get("/api/v1/urls/missing-" + UUID.randomUUID()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("NOT_FOUND"));
	}

	@Test
	void getMetadataForExpiredAliasReturns404() throws Exception {
		String alias = "exp-meta-" + UUID.randomUUID().toString().substring(0, 8);
		Instant now = clock.instant();

		shortUrlRepository.saveAndFlush(
				com.urlshortener.domain.ShortUrl.builder()
						.alias(alias)
						.originalUrl("https://example.com/expired-meta")
						.accessCount(0L)
						.createdAt(now.minusSeconds(120))
						.expiresAt(now.minusSeconds(60))
						.build());

		mockMvc.perform(get("/api/v1/urls/" + alias))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("NOT_FOUND"));
	}

}
