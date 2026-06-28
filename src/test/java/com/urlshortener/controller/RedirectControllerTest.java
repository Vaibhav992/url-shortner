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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@Transactional
class RedirectControllerTest {

	@DynamicPropertySource
	static void registerPostgres(DynamicPropertyRegistry registry) {
		IntegrationTestSupport.registerPostgresProperties(registry);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private Clock clock;

	@MockitoSpyBean
	private com.urlshortener.repository.ShortUrlRepository shortUrlRepository;

	@MockitoSpyBean
	private com.urlshortener.service.AccessCountService accessCountService;

	@Test
	void redirectValidAliasReturns302WithLocation() throws Exception {
		String alias = "redirect-" + UUID.randomUUID().toString().substring(0, 8);

		mockMvc.perform(post("/api/v1/urls")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"originalUrl":"https://example.com/target","customAlias":"%s","expiresAt":"2030-01-01T00:00:00Z"}
						""".formatted(alias)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/" + alias))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "https://example.com/target"));
	}

	@Test
	void redirectUnknownAliasReturns404() throws Exception {
		mockMvc.perform(get("/missing-" + UUID.randomUUID()))
				.andExpect(status().isNotFound());
	}

	@Test
	void redirectExpiredAliasReturns404() throws Exception {
		String alias = "expired-" + UUID.randomUUID().toString().substring(0, 8);
		Instant now = clock.instant();

		shortUrlRepository.saveAndFlush(
				com.urlshortener.domain.ShortUrl.builder()
						.alias(alias)
						.originalUrl("https://example.com/expired")
						.accessCount(0L)
						.createdAt(now.minusSeconds(120))
						.expiresAt(now.minusSeconds(60))
						.build());

		mockMvc.perform(get("/" + alias))
				.andExpect(status().isNotFound());
	}

	@Test
	void secondRedirectUsesCache() throws Exception {
		String alias = "cached-" + UUID.randomUUID().toString().substring(0, 8);

		mockMvc.perform(post("/api/v1/urls")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"originalUrl":"https://example.com/cached","customAlias":"%s","expiresAt":"2030-01-01T00:00:00Z"}
						""".formatted(alias)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/" + alias)).andExpect(status().isFound());
		mockMvc.perform(get("/" + alias)).andExpect(status().isFound());

		verify(shortUrlRepository, times(1)).findByAlias(alias);
	}

	@Test
	void redirectTriggersAsyncAccessCountIncrement() throws Exception {
		String alias = "async-count-" + UUID.randomUUID().toString().substring(0, 8);

		mockMvc.perform(post("/api/v1/urls")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"originalUrl":"https://example.com/async-count","customAlias":"%s","expiresAt":"2030-01-01T00:00:00Z"}
						""".formatted(alias)))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/" + alias))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "https://example.com/async-count"));

		verify(accessCountService, timeout(5000).times(1)).incrementAccessCount(alias);
	}

}
