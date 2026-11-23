package com.rookies6.DoQFlowAI.controller;

import com.rookies6.DoQFlowAI.dto.response.ApiResponse;
import com.rookies6.DoQFlowAI.dto.response.VersionUploadResponse;
import com.rookies6.DoQFlowAI.entity.AppUser;
import com.rookies6.DoQFlowAI.security.CurrentUser;
import com.rookies6.DoQFlowAI.service.UrlImportService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DocumentImportController {
	private final UrlImportService imports;

	@PostMapping("/documents/{documentId}/versions/from-url")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<VersionUploadResponse> importUrl(@CurrentUser AppUser me,
			@PathVariable @Min(1) Long documentId, @Valid @RequestBody ImportRequest body) {
		return ApiResponse.ok(imports.importVersion(me, documentId, body.url()));
	}

	public record ImportRequest(@NotBlank @Size(max = 2048) String url) { }
}
