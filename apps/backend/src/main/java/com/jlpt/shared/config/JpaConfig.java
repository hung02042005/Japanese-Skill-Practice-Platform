/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Kept separate from {@code JlptApplication} so web test slices (e.g. {@code @WebMvcTest})
 * can exclude it via {@code TypeExcludeFilter} — annotations placed directly on the
 * {@code @SpringBootApplication} class are processed regardless of test slice.
 */
@Configuration
@EntityScan(basePackages = "com.jlpt")
@EnableJpaRepositories(basePackages = "com.jlpt")
public class JpaConfig {
}
