/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.controller;

import com.jlpt.feature.support.dto.TicketDetailResponse;
import com.jlpt.feature.support.dto.TicketReplyRequest;
import com.jlpt.feature.support.dto.TicketReplyResponse;
import com.jlpt.feature.support.dto.TicketRequest;
import com.jlpt.feature.support.dto.TicketResponse;
import com.jlpt.feature.support.service.SupportTicketService;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@Validated
public class SupportController {

    private final SupportTicketService supportTicketService;

    // ── UC-29: Create ticket ─────────────────────────────────────────────────

    @PostMapping("/tickets")
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @AuthenticationPrincipal UserDetailsImpl principal, @Valid @RequestBody TicketRequest req) {
        TicketResponse result =
                supportTicketService.createTicket(principal.getStudentUser().getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<TicketResponse>builder()
                        .status(201)
                        .message("Ticket hỗ trợ đã được tạo thành công")
                        .data(result)
                        .build());
    }

    // ── UC-29: My tickets ────────────────────────────────────────────────────

    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyTickets(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page phải >= 0") int page,
            @RequestParam(defaultValue = "10")
                    @Min(value = 1, message = "size phải >= 1")
                    @Max(value = 100, message = "size tối đa 100")
                    int size) {
        Long studentId = principal.getStudentUser().getId();
        Page<TicketResponse> result = supportTicketService.getMyTickets(studentId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages())));
    }

    // ── UC-29: Ticket detail ─────────────────────────────────────────────────

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketDetail(
            @AuthenticationPrincipal UserDetailsImpl principal, @PathVariable Long ticketId) {
        TicketDetailResponse result = supportTicketService.getStudentTicketDetail(
                ticketId, principal.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── UC-29: Reply to ticket ───────────────────────────────────────────────

    @PostMapping("/tickets/{ticketId}/reply")
    public ResponseEntity<ApiResponse<TicketReplyResponse>> replyToTicket(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketReplyRequest req) {
        TicketReplyResponse result = supportTicketService.addStudentReply(
                ticketId, principal.getStudentUser().getId(), req);
        return ResponseEntity.ok(ApiResponse.success("Gửi phản hồi thành công", result));
    }

    // ── UC-29: Close own ticket ──────────────────────────────────────────────

    @PostMapping("/tickets/{ticketId}/close")
    public ResponseEntity<ApiResponse<TicketResponse>> closeTicket(
            @AuthenticationPrincipal UserDetailsImpl principal, @PathVariable Long ticketId) {
        TicketResponse result = supportTicketService.closeStudentTicket(
                ticketId, principal.getStudentUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Ticket đã được đóng", result));
    }
}
