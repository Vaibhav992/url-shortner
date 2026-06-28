package com.urlshortener.service;

import com.urlshortener.dto.CreateUrlRequestDto;
import com.urlshortener.exception.AliasConflictException;
import com.urlshortener.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("prod")
class AliasConcurrencyIntegrationTest {

	private static final int THREAD_COUNT = 8;

	@DynamicPropertySource
	static void registerPostgres(DynamicPropertyRegistry registry) {
		IntegrationTestSupport.registerPostgresProperties(registry);
	}

	@Autowired
	private UrlService urlService;

	@Test
	void concurrentCustomAliasCreationHasSingleWinner() throws Exception {
		String alias = "race-" + UUID.randomUUID().toString().substring(0, 8);
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger created = new AtomicInteger();
		AtomicInteger conflict = new AtomicInteger();
		AtomicInteger unexpected = new AtomicInteger();

		List<Future<?>> futures = new ArrayList<>();
		for (int i = 0; i < THREAD_COUNT; i++) {
			int index = i;
			futures.add(executor.submit(() -> {
				ready.countDown();
				try {
					start.await();
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
					return;
				}
				CreateUrlRequestDto request = new CreateUrlRequestDto();
				request.setOriginalUrl("https://example.com/concurrent-" + index);
				request.setCustomAlias(alias);
				try {
					urlService.createUrl(request);
					created.incrementAndGet();
				}
				catch (AliasConflictException ex) {
					conflict.incrementAndGet();
				}
				catch (Exception ex) {
					unexpected.incrementAndGet();
				}
			}));
		}

		ready.await(10, TimeUnit.SECONDS);
		start.countDown();

		for (Future<?> future : futures) {
			future.get(30, TimeUnit.SECONDS);
		}
		executor.shutdown();
		assertThat(executor.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

		assertThat(created).hasValue(1);
		assertThat(conflict).hasValue(THREAD_COUNT - 1);
		assertThat(unexpected).hasValue(0);
	}

}
