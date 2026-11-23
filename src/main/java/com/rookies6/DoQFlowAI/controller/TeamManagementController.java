package com.rookies6.DoQFlowAI.controller;

import com.rookies6.DoQFlowAI.dto.request.DocumentListRequest;
import com.rookies6.DoQFlowAI.dto.response.ApiResponse;
import com.rookies6.DoQFlowAI.dto.response.DocumentSummaryResponse;
import com.rookies6.DoQFlowAI.dto.response.PageResponse;
import com.rookies6.DoQFlowAI.entity.AppUser;
import com.rookies6.DoQFlowAI.security.CurrentUser;
import com.rookies6.DoQFlowAI.service.TeamManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

/** 사원/팀장 역할을 유지하는 팀장 전용 기능. 실시간 토글 관리와 무관하다. */
@RestController
@RequiredArgsConstructor
public class TeamManagementController {
	private final TeamManagementService management;
	@GetMapping("/management/documents")
	public ApiResponse<PageResponse<DocumentSummaryResponse>> list(@CurrentUser AppUser me,
			@Valid @ModelAttribute DocumentListRequest request) {
		return ApiResponse.ok(management.list(me, request));
	}
}
