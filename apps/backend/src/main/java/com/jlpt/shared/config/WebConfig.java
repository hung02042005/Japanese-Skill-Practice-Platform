/* (c) JLPT E-Learning Platform */
package com.jlpt.shared.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Phục vụ file media tĩnh (audio Kana, ...) từ thư mục upload ngoài.
 * ADR-006: file ở /uploads (dev) hoặc S3 (prod), DB chỉ lưu path.
 *
 * Map: GET /api/files/audio/kana/a.mp3  ->  {upload-dir}/audio/kana/a.mp3
 * Đặt dưới /api/** để dùng chung proxy /api của frontend (Vite/reverse proxy).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/api/files/**")
                .addResourceLocations(uploadPath.toUri().toString());
    }
}
