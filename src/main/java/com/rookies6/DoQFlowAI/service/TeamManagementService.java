package com.rookies6.DoQFlowAI.service;

import com.rookies6.DoQFlowAI.dto.request.DocumentListRequest;
import com.rookies6.DoQFlowAI.dto.response.DocumentSummaryResponse;
import com.rookies6.DoQFlowAI.dto.response.PageResponse;
import com.rookies6.DoQFlowAI.entity.AppUser;
import com.rookies6.DoQFlowAI.repository.DocumentRepository;
import com.rookies6.DoQFlowAI.security.AccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamManagementService {
	private final AccessPolicy access;
	private final DocumentRepository documents;
	public PageResponse<DocumentSummaryResponse> list(AppUser me, DocumentListRequest request) {
		access.registerOrThrow(me.getId()); // DB의 현재 직급으로 확인한다.
		return PageResponse.of(documents.findByUploadedById(me.getId(), request.toPageable()),
				document -> DocumentSummaryResponse.of(document, me.getId()));
	}
}
