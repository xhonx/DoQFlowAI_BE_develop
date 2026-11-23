package com.rookies6.DoQFlowAI.controller;

import com.rookies6.DoQFlowAI.entity.AppUser;
import com.rookies6.DoQFlowAI.security.CurrentUser;
import com.rookies6.DoQFlowAI.service.FileDownloadService;
import jakarta.validation.constraints.Min;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FileDownloadController {
	private final FileDownloadService downloads;

	@GetMapping("/documents/{documentId}/versions/{versionNo}/download")
	public ResponseEntity<Resource> download(@CurrentUser AppUser me,
			@PathVariable @Min(1) Long documentId, @PathVariable @Min(1) int versionNo) {
		var file = downloads.download(me, documentId, versionNo);
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.contentLength(file.size())
				.cacheControl(CacheControl.noStore())
				.header("X-Content-Type-Options", "nosniff")
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
						.filename(file.filename(), StandardCharsets.UTF_8).build().toString())
				.body(file.resource());
	}
}
