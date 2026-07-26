/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.speaking.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jlpt.shared.exception.BadRequestException;
import com.jlpt.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class SpeakingAudioStorageServiceTest {

    @InjectMocks
    private SpeakingAudioStorageService service;

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
    void store_fileTooLarge_throwsBusinessException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(20L * 1024 * 1024);
        assertThrows(BusinessException.class, () -> service.store(file, 1L));
    }

    @Test
    void store_invalidContentType_throwsBusinessException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1000L);
        when(file.getContentType()).thenReturn("text/plain");

        assertThrows(BusinessException.class, () -> service.store(file, 1L));
    }

    @Test
    void store_validFile_success() throws Exception {
        ReflectionTestUtils.setField(service, "uploadDir", "target/test-uploads");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1000L);
        when(file.getContentType()).thenReturn("audio/wav");

        SpeakingAudioStorageService.StoredAudio res = service.store(file, 1L);
        assertNotNull(res);
        assertNotNull(res.url());
        assertNotNull(res.path());
    }
}
