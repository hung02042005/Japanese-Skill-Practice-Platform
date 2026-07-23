/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.service;

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
 * Lưu file ghi âm speaking ra thư mục upload ngoài (ADR-006 / LESSON-002 — KHÔNG lưu BLOB).
 * Trả về cả URL public (phục vụ qua {@code /api/files/**}) lẫn {@link Path} tuyệt đối để engine đọc.
 */
@Service
public class SpeakingAudioStorageService {

    private static final long MAX_SIZE = 10L * 1024 * 1024; // 10MB (UC-13 BR)
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "audio/webm",
            "video/webm",
            "audio/mpeg",
            "audio/mp3",
            "audio/wav",
            "audio/wave",
            "audio/x-wav",
            "audio/ogg",
            "audio/mp4",
            "audio/m4a",
            "audio/x-m4a",
            "audio/aac");

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    /** Kết quả lưu file: URL để trả client/lưu DB + đường dẫn thật cho engine xử lý. */
    public record StoredAudio(String url, Path path) {}

    public StoredAudio store(MultipartFile file, Long studentId) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File ghi âm không được để trống");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(400, "FILE_TOO_LARGE", "File ghi âm tối đa 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new BusinessException(
                    400, "INVALID_FILE_TYPE", "Định dạng ghi âm không hợp lệ (chỉ chấp nhận webm/mp3/wav)");
        }

        String baseType = contentType.split(";")[0].trim().toLowerCase();
        if (!ALLOWED_TYPES.contains(baseType)) {
            throw new BusinessException(
                    400, "INVALID_FILE_TYPE", "Định dạng ghi âm không hợp lệ (chỉ chấp nhận webm/mp3/wav)");
        }

        String ext =
                switch (baseType) {
                    case "audio/mpeg", "audio/mp3" -> ".mp3";
                    case "audio/wav", "audio/wave", "audio/x-wav" -> ".wav";
                    case "audio/ogg" -> ".ogg";
                    case "audio/mp4", "audio/m4a", "audio/x-m4a", "audio/aac" -> ".m4a";
                    default -> ".webm";
                };
        String filename = "speaking-" + studentId + "-" + System.currentTimeMillis() + ext;

        try {
            Path dir = Paths.get(uploadDir, "speaking").toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            file.transferTo(target);
            return new StoredAudio("/api/files/speaking/" + filename, target);
        } catch (IOException e) {
            throw new BusinessException(500, "UPLOAD_FAILED", "Không thể lưu file ghi âm");
        }
    }
}
