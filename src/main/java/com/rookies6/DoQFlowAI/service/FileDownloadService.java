package com.rookies6.DoQFlowAI.service;

import com.rookies6.DoQFlowAI.entity.AppUser;
import com.rookies6.DoQFlowAI.entity.DocumentVersion;
import com.rookies6.DoQFlowAI.exception.VersionNotFoundException;
import com.rookies6.DoQFlowAI.repository.DocumentVersionRepository;
import com.rookies6.DoQFlowAI.security.AccessPolicy;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 원본 다운로드. 클라이언트의 경로/파일명은 받지 않으며 문서 열람 권한을 재사용한다. */
@Service
@Transactional(readOnly = true)
public class FileDownloadService {
	private final AccessPolicy access;
	private final DocumentVersionRepository versions;
	private final Path baseDir;

	public FileDownloadService(AccessPolicy access, DocumentVersionRepository versions,
			@Value("${app.upload-dir}") String uploadDir) {
		this.access = access;
		this.versions = versions;
		this.baseDir = Path.of(uploadDir).toAbsolutePath().normalize();
	}

	public Download download(AppUser me, Long documentId, int versionNo) {
		access.readOrNotFound(me.getId(), documentId);
		DocumentVersion version = versions.findByDocumentIdAndVersionNo(documentId, versionNo)
				.orElseThrow(VersionNotFoundException::new);
		String stored = version.getStoredFilename();
		if (stored == null || stored.isBlank() || stored.equals(".") || stored.equals("..")
				|| stored.contains("/") || stored.contains("\\") || stored.contains(":")) {
			throw new VersionNotFoundException();
		}
		String filename = downloadFilename(version.getOriginalFilename());
		try {
			Path root = baseDir.toRealPath();
			Path expectedDir = root.resolve(documentId.toString()).resolve(Integer.toString(versionNo));
			Path target = expectedDir.resolve(stored).normalize();
			// 다른 문서나 외부 파일로 향하는 심볼릭 링크도 거절한다.
			if (!target.getParent().equals(expectedDir) || !target.toRealPath().equals(target)
					|| !Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
				throw new VersionNotFoundException();
			}
			SeekableByteChannel channel = Files.newByteChannel(target,
					StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
			try {
				long size = channel.size();
				return new Download(new InputStreamResource(Channels.newInputStream(channel)), filename, size);
			} catch (IOException | RuntimeException e) {
				channel.close();
				throw e;
			}
		} catch (IOException | java.nio.file.InvalidPathException e) {
			// 파일 유무/파일시스템 상세를 응답에 노출하지 않는다.
			throw new VersionNotFoundException();
		}
	}

	static String downloadFilename(String original) {
		if (original == null || original.isBlank()) return "download";
		String name = original.replace('\\', '/');
		name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "_");
		return name.isBlank() || name.equals(".") || name.equals("..") ? "download" : name;
	}

	/** 응답 변환기가 스트림을 닫는다. 전체 파일을 메모리에 적재하지 않는다. */
	public record Download(Resource resource, String filename, long size) { }
}
