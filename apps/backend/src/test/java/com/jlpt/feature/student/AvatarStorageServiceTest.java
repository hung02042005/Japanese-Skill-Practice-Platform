/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.student;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jlpt.shared.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AvatarStorageServiceTest {

    @InjectMocks
    private AvatarStorageService service;

    @Test
    void store_nullFile_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> service.store(null, 1L));
    }

    @Test
    void store_emptyFile_throwsBadRequest() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        assertThrows(BadRequestException.class, () -> service.store(file, 1L));
    }

    @Test
    void store_fileTooLarge_throwsBadRequest() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(10L * 1024 * 1024);
        assertThrows(BadRequestException.class, () -> service.store(file, 1L));
    }

    @Test
    void store_invalidContentType_throwsBadRequest() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1000L);
        when(file.getContentType()).thenReturn("application/pdf");

        assertThrows(BadRequestException.class, () -> service.store(file, 1L));
    }

    @Test
    void store_validImage_success() throws Exception {
        ReflectionTestUtils.setField(service, "uploadDir", "target/test-avatars");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1000L);
        when(file.getContentType()).thenReturn("image/png");

        String url = service.store(file, 1L);
        assertNotNull(url);
        assertTrue(url.startsWith("/api/files/avatars/student-1-"));
    }
}
