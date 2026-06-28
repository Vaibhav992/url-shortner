package com.urlshortener.exception;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@Transactional
class GlobalExceptionHandlerTest {

	@DynamicPropertySource
	static void registerPostgres(DynamicPropertyRegistry registry) {
		IntegrationTestSupport.registerPostgresProperties(registry);
	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	void validationErrorReturnsConsistentJsonShape() throws Exception {
		mockMvc.perform(post("/api/v1/urls")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"originalUrl":""}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.message").value("originalUrl is required"));
	}

	@Test
	void notFoundErrorReturnsConsistentJsonShape() throws Exception {
		mockMvc.perform(get("/api/v1/urls/does-not-exist-alias"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("NOT_FOUND"))
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void malformedJsonReturnsConsistentJsonShape() throws Exception {
		mockMvc.perform(post("/api/v1/urls")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{invalid-json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("Malformed JSON request body"));
	}

}
