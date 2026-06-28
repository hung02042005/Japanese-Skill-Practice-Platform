/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import com.jlpt.shared.exception.BadRequestException;
import com.jlpt.shared.exception.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Lưu ảnh đại diện học viên ra thư mục upload ngoài (ADR-006 / LESSON-002 — KHÔNG lưu BLOB trong DB).
 * Trả về URL public phục vụ qua {@code /api/files/**} (xem {@code WebConfig}).
 */
@Service
public class AvatarStorageService {

    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/png", "image/jpeg", "image/jpg", "image/webp");

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public String store(MultipartFile file, Long studentId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Ảnh đại diện không được để trống");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BadRequestException("Ảnh đại diện tối đa 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Định dạng ảnh không hợp lệ (chỉ chấp nhận PNG/JPG/WEBP)");
        }

        String ext = switch (contentType.toLowerCase()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String filename = "student-" + studentId + "-" + System.currentTimeMillis() + ext;

        try {
            Path dir = Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename));
        } catch (IOException e) {
            throw new BusinessException(500, "UPLOAD_FAILED", "Không thể lưu ảnh đại diện");
        }
        return "/api/files/avatars/" + filename;
    }
}
