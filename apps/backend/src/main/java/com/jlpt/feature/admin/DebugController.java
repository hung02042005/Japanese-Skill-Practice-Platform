/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.admin;

import com.jlpt.shared.email.EmailOutbox;
import com.jlpt.shared.email.EmailOutboxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug-smtp")
@RequiredArgsConstructor
public class DebugController {

    private final EmailOutboxRepository emailOutboxRepository;

    @GetMapping("/outbox")
    public ResponseEntity<List<EmailOutbox>> getOutbox() {
        List<EmailOutbox> outbox = emailOutboxRepository
                .findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
        return ResponseEntity.ok(outbox);
    }
}
