package com.urlshortener.repository;

import com.urlshortener.domain.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

	Optional<ShortUrl> findByAlias(String alias);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE ShortUrl s SET s.accessCount = s.accessCount + 1 WHERE s.alias = :alias")
	int incrementAccessCount(@Param("alias") String alias);

}
