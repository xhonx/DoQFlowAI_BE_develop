package com.rookies6.DoQFlowAI.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.rookies6.DoQFlowAI.entity.AppUser;
import com.rookies6.DoQFlowAI.entity.DocumentVersion;
import com.rookies6.DoQFlowAI.exception.DocumentNotFoundException;
import com.rookies6.DoQFlowAI.exception.VersionNotFoundException;
import com.rookies6.DoQFlowAI.repository.DocumentVersionRepository;
import com.rookies6.DoQFlowAI.security.AccessPolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileDownloadServiceTest {
	@TempDir Path root;
	private AccessPolicy access;
	private DocumentVersionRepository versions;
	private FileDownloadService service;
	private AppUser me;

	@BeforeEach
	void setUp() {
		access = mock(AccessPolicy.class);
		versions = mock(DocumentVersionRepository.class);
		me = mock(AppUser.class);
		when(me.getId()).thenReturn(5L);
		service = new FileDownloadService(access, versions, root.toString());
	}

	private void version(String stored, String original) {
		when(versions.findByDocumentIdAndVersionNo(7L, 2)).thenReturn(Optional.of(
				DocumentVersion.builder().versionNo(2).storedFilename(stored).originalFilename(original).build()));
	}

	@Test
	void returnsOriginalBytesEvenWhenAiHasNotCompleted() throws Exception {
		version("stored.pdf", "원본 문서.pdf");
		Path dir = Files.createDirectories(root.resolve("7/2"));
		byte[] bytes = new byte[] {0, 1, 2, -1, 10};
		Files.write(dir.resolve("stored.pdf"), bytes);
		var result = service.download(me, 7L, 2);
		try (var stream = result.resource().getInputStream()) {
			assertThat(stream.readAllBytes()).isEqualTo(bytes);
		}
		assertThat(result.filename()).isEqualTo("원본 문서.pdf");
		assertThat(result.size()).isEqualTo(bytes.length);
		verify(access).readOrNotFound(5L, 7L);
	}

	@Test
	void deniesBeforeLookingUpVersionOrFile() {
		when(access.readOrNotFound(5L, 7L)).thenThrow(new DocumentNotFoundException());
		assertThatThrownBy(() -> service.download(me, 7L, 2)).isInstanceOf(DocumentNotFoundException.class);
		verifyNoInteractions(versions);
	}

	@Test
	void missingVersionAndMissingFileAreBothNotFound() {
		assertThatThrownBy(() -> service.download(me, 7L, 2)).isInstanceOf(VersionNotFoundException.class);
		version("missing.pdf", "missing.pdf");
		assertThatThrownBy(() -> service.download(me, 7L, 2)).isInstanceOf(VersionNotFoundException.class);
	}

	@Test
	void rejectsStoredPathsAndDirectories() throws Exception {
		for (String stored : new String[] {"../secret", "sub/file", "C:\\secret", "..", ".", "file:stream"}) {
			version(stored, "test");
			assertThatThrownBy(() -> service.download(me, 7L, 2)).isInstanceOf(VersionNotFoundException.class);
		}
		version("directory", "test");
		Files.createDirectories(root.resolve("7/2/directory"));
		assertThatThrownBy(() -> service.download(me, 7L, 2)).isInstanceOf(VersionNotFoundException.class);
	}

	@Test
	void stripsPathAndControlCharactersFromDownloadName() {
		assertThat(FileDownloadService.downloadFilename("C:\\fakepath\\한글.pdf")).isEqualTo("한글.pdf");
		assertThat(FileDownloadService.downloadFilename("../a\r\nb.txt")).isEqualTo("a__b.txt");
		assertThat(FileDownloadService.downloadFilename("..")).isEqualTo("download");
	}
}
