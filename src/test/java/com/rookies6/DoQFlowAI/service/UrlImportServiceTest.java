package com.rookies6.DoQFlowAI.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.rookies6.DoQFlowAI.entity.AppUser;
import com.rookies6.DoQFlowAI.exception.ForbiddenException;
import com.rookies6.DoQFlowAI.security.AccessPolicy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.multipart.MultipartFile;

class UrlImportServiceTest {
	@Test void rejectsUnauthorizedBeforeNetworkRequest() {
		AccessPolicy access = mock(AccessPolicy.class);
		UrlFetchClient fetch = mock(UrlFetchClient.class);
		VersionService versions = mock(VersionService.class);
		AppUser me = mock(AppUser.class);
		when(me.getId()).thenReturn(1L);
		doThrow(new ForbiddenException()).when(access).registerOrThrow(1L);
		var service = new UrlImportService(access, new UrlImportPolicy("files.example.com"), fetch, versions);
		assertThatThrownBy(() -> service.importVersion(me, 7L, "https://files.example.com/report.txt"))
				.isInstanceOf(ForbiddenException.class);
		verifyNoInteractions(fetch, versions);
	}
	@Test void reusesUploadPipelineAfterAccessCheck() throws Exception {
		AccessPolicy access = mock(AccessPolicy.class);
		UrlFetchClient fetch = mock(UrlFetchClient.class);
		VersionService versions = mock(VersionService.class);
		AppUser me = mock(AppUser.class);
		when(me.getId()).thenReturn(1L);
		URI uri = URI.create("https://files.example.com/report.txt");
		when(fetch.fetch(uri)).thenReturn("샘플 본문".getBytes(StandardCharsets.UTF_8));
		new UrlImportService(access, new UrlImportPolicy("files.example.com"), fetch, versions)
				.importVersion(me, 7L, uri.toString());
		verify(access).writeOrThrow(1L, 7L);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<java.util.List<MultipartFile>> files = ArgumentCaptor.forClass(java.util.List.class);
		verify(versions).uploadBatch(eq(me), eq(7L), files.capture());
		assertThat(files.getValue().get(0).getOriginalFilename()).isEqualTo("report.txt");
		assertThat(files.getValue().get(0).getBytes()).isEqualTo("샘플 본문".getBytes(StandardCharsets.UTF_8));
	}
}
