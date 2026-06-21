/* (c) JLPT E-Learning Platform */
package com.jlpt.feature.dictionary.controller;

import com.jlpt.feature.dictionary.dto.SearchResponse;
import com.jlpt.feature.dictionary.service.DictionaryService;
import com.jlpt.shared.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dictionary")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentDictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchResponse>> search(
            @RequestParam String q,
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(required = false) String type) {
        SearchResponse response = dictionaryService.search(q, jlptLevel, type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
