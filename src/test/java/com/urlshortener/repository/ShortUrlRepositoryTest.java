package com.urlshortener.repository;

import com.urlshortener.domain.ShortUrl;
import com.urlshortener.mapper.ShortUrlMapper;
import com.urlshortener.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("prod")
@Transactional
class ShortUrlRepositoryTest {

	private static final Instant NOW = Instant.parse("2026-06-28T12:00:00Z");

	@DynamicPropertySource
	static void registerPostgres(DynamicPropertyRegistry registry) {
		IntegrationTestSupport.registerPostgresProperties(registry);
	}

	@Autowired
	private ShortUrlRepository shortUrlRepository;

	@Autowired
	private ShortUrlMapper shortUrlMapper;

	@Test
	void saveAndFindByAlias() {
		String alias = uniqueAlias("abc");
		ShortUrl saved = shortUrlRepository.saveAndFlush(sampleUrl(alias));

		assertThat(saved.getId()).isNotNull();
		assertThat(shortUrlRepository.findByAlias(alias))
				.isPresent()
				.get()
				.satisfies(url -> {
					assertThat(url.getOriginalUrl()).isEqualTo("https://example.com");
					assertThat(url.getAccessCount()).isZero();
					assertThat(url.getCreatedAt()).isEqualTo(saved.getCreatedAt());
					assertThat(url.getExpiresAt()).isEqualTo(saved.getExpiresAt());
				});
	}

	@Test
	void duplicateAliasThrowsOnSecondInsert() {
		String alias = uniqueAlias("taken");
		shortUrlRepository.saveAndFlush(sampleUrl(alias));

		ShortUrl duplicate = sampleUrl(alias);
		duplicate.setOriginalUrl("https://other.com");

		assertThatThrownBy(() -> shortUrlRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void incrementAccessCountUpdatesAtomically() {
		String alias = uniqueAlias("click");
		shortUrlRepository.saveAndFlush(sampleUrl(alias));

		int updated = shortUrlRepository.incrementAccessCount(alias);
		int updatedAgain = shortUrlRepository.incrementAccessCount(alias);

		assertThat(updated).isEqualTo(1);
		assertThat(updatedAgain).isEqualTo(1);
		assertThat(shortUrlRepository.findByAlias(alias))
				.get()
				.extracting(ShortUrl::getAccessCount)
				.isEqualTo(2L);
	}

	private ShortUrl sampleUrl(String alias) {
		return shortUrlMapper.toEntity(alias, "https://example.com", NOW, NOW.plusSeconds(86_400));
	}

	private static String uniqueAlias(String prefix) {
		return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
	}

}
