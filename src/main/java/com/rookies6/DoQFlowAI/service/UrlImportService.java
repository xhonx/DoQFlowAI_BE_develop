package com.rookies6.DoQFlowAI.service;

import com.rookies6.DoQFlowAI.dto.response.VersionUploadResponse;
import com.rookies6.DoQFlowAI.entity.AppUser;
import com.rookies6.DoQFlowAI.security.AccessPolicy;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** 네트워크 대기 중 DB 트랜잭션을 잡지 않는다. 저장 직전에 기존 업로드가 권한을 다시 검사한다. */
@Service
@RequiredArgsConstructor
public class UrlImportService {
	private final AccessPolicy access;
	private final UrlImportPolicy policy;
	private final UrlFetchClient fetcher;
	private final VersionService versions;

	public VersionUploadResponse importVersion(AppUser me, Long documentId, String url) {
		access.registerOrThrow(me.getId());
		access.writeOrThrow(me.getId(), documentId);
		URI uri = policy.validate(url);
		String path = uri.getPath();
		String filename = path == null ? "" : path.substring(path.lastIndexOf('/') + 1);
		byte[] body = fetcher.fetch(uri);
		// 기존 확장자·매직넘버·UTF-8·중복·파일명·크기 검증, 저장, 감사 로그, AI 후처리 모두 재사용.
		return versions.uploadBatch(me, documentId, List.of(new ImportedFile(filename, body)));
	}

	private record ImportedFile(String filename, byte[] bytes) implements MultipartFile {
		@Override public String getName() { return "files"; }
		@Override public String getOriginalFilename() { return filename; }
		@Override public String getContentType() { return "application/octet-stream"; }
		@Override public boolean isEmpty() { return bytes.length == 0; }
		@Override public long getSize() { return bytes.length; }
		@Override public byte[] getBytes() { return bytes.clone(); }
		@Override public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }
		@Override public void transferTo(File destination) throws IOException { Files.write(destination.toPath(), bytes); }
	}
}
