package com.rookies6.DoQFlowAI.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.rookies6.DoQFlowAI.exception.BusinessException;
import com.rookies6.DoQFlowAI.exception.ErrorCode;
import java.io.ByteArrayInputStream;
import java.net.URI;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.jupiter.api.Test;

class UrlFetchClientTest {
	@SuppressWarnings("unchecked")
	private UrlFetchClient client(int status, byte[] bytes, long length) throws Exception {
		CloseableHttpClient http = mock(CloseableHttpClient.class);
		when(http.execute(any(HttpGet.class), any(HttpClientResponseHandler.class))).thenAnswer(invocation -> {
			var response = new BasicClassicHttpResponse(status);
			response.setEntity(new InputStreamEntity(new ByteArrayInputStream(bytes), length, ContentType.TEXT_PLAIN));
			HttpClientResponseHandler<byte[]> handler = invocation.getArgument(1);
			return handler.handleResponse(response);
		});
		return new UrlFetchClient(http);
	}
	@Test void refusesRedirectAndUnknownLengthOversizedBody() throws Exception {
		UrlFetchClient redirect = client(302, new byte[0], 0);
		try {
			assertThatThrownBy(() -> redirect.fetch(URI.create("https://files.example.com/a.txt")))
					.isInstanceOf(BusinessException.class);
		} finally { redirect.close(); }
		UrlFetchClient large = client(200, new byte[20 * 1024 * 1024 + 1], -1);
		try {
			assertThatThrownBy(() -> large.fetch(URI.create("https://files.example.com/a.txt")))
					.isInstanceOfSatisfying(BusinessException.class, e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.FILE_TOO_LARGE));
		} finally { large.close(); }
	}
	@Test void returnsSuccessfulBytes() throws Exception {
		UrlFetchClient success = client(200, new byte[] {1, 2, 3}, 3);
		try { assertThat(success.fetch(URI.create("https://files.example.com/a.txt"))).containsExactly(1, 2, 3); }
		finally { success.close(); }
	}
}
