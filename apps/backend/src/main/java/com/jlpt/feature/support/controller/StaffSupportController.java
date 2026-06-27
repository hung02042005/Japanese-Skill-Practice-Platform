/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.support.controller;

import com.jlpt.feature.support.dto.AssignTicketRequest;
import com.jlpt.feature.support.dto.TicketResponse;
import com.jlpt.feature.support.service.SupportTicketService;
import com.jlpt.shared.common.ApiResponse;
import com.jlpt.feature.support.dto.TicketReplyRequest;
import com.jlpt.feature.support.dto.TicketDetailResponse;
import com.jlpt.feature.support.dto.TicketReplyResponse;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Staff / Staff Manager — xu ly ticket ho tro.
 * Luong: Student tao (open) -> Staff Manager duyet + gan (assigned)
 *        -> Staff duoc giao ho tro (in_progress) -> dong (resolved).
 */
@RestController
@RequestMapping("/api/staff/tickets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')")
public class StaffSupportController {

    private final SupportTicketService supportTicketService;

    // -- Danh sach ticket (loc theo status/category/priority/tu khoa) ----------

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TicketResponse> result =
                supportTicketService.getAllTickets(status, category, priority, q, page, size);
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages())));
    }

    // -- Chi tiet ticket (Staff — khong gioi han owner) -----------------------

    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketDetail(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(supportTicketService.getStaffTicketDetail(ticketId)));
    }

    // -- Staff Manager duyet + gan ticket cho 1 staff --------------------------

    @PostMapping("/{ticketId}/assign")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
            Authentication authentication,
            @PathVariable Long ticketId,
            @Valid @RequestBody AssignTicketRequest req) {
        TicketResponse result = supportTicketService.assignTicket(
                ticketId, req.getAssignToStaffId(), authentication.getName(), false);
        return ResponseEntity.ok(ApiResponse.success("Da giao ticket cho nhan vien", result));
    }

    // -- Staff duoc giao (hoac Staff Manager) phan hoi ticket ------------------

    @PostMapping("/{ticketId}/reply")
    public ResponseEntity<ApiResponse<TicketReplyResponse>> replyToTicket(
            Authentication authentication,
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketReplyRequest req) {
        TicketReplyResponse result =
                supportTicketService.addStaffReply(ticketId, authentication.getName(), req);
        return ResponseEntity.ok(ApiResponse.success("Gui phan hoi thanh cong", result));
    }

    // -- Dong ticket (Staff Manager / Staff xu ly) -----------------------------

    @PostMapping("/{ticketId}/close")
    public ResponseEntity<ApiResponse<TicketResponse>> closeTicket(
            Authentication authentication,
            @PathVariable Long ticketId) {
        TicketResponse result = supportTicketService.closeTicket(ticketId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Ticket da duoc dong", result));
    }
}
