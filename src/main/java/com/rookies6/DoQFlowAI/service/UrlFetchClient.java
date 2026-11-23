package com.rookies6.DoQFlowAI.service;

import com.rookies6.DoQFlowAI.exception.BusinessException;
import com.rookies6.DoQFlowAI.exception.ErrorCode;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.stereotype.Component;

/** TLS 검증 유지, 프록시/리다이렉트/쿠키/인증 전달 없이 공개 원본을 가져온다. */
@Component
public class UrlFetchClient {
	private static final int LIMIT = 20 * 1024 * 1024;
	private final CloseableHttpClient client;
	private final ScheduledExecutorService deadlines = Executors.newSingleThreadScheduledExecutor(task -> {
		Thread thread = new Thread(task, "url-import-deadline");
		thread.setDaemon(true);
		return thread;
	});

	public UrlFetchClient() {
		var resolver = new SystemDefaultDnsResolver() {
			@Override public InetAddress[] resolve(String host) throws UnknownHostException {
				InetAddress[] addresses = super.resolve(host);
				if (addresses.length == 0 || Arrays.stream(addresses).anyMatch(a -> !UrlImportPolicy.isPublicAddress(a))) {
					throw new UnknownHostException("URL import blocked a non-public address");
				}
				return addresses; // 실제 소켓이 이 검증된 주소로 연결되어 DNS 재조회 우회를 방지한다.
			}
		};
		var connections = PoolingHttpClientConnectionManagerBuilder.create().setDnsResolver(resolver)
				.setMaxConnTotal(4).setMaxConnPerRoute(2)
				.setDefaultConnectionConfig(ConnectionConfig.custom().setConnectTimeout(Timeout.ofSeconds(3))
						.setSocketTimeout(Timeout.ofSeconds(10)).build()).build();
		client = HttpClients.custom().setConnectionManager(connections)
				.disableRedirectHandling().disableAutomaticRetries().disableCookieManagement().disableContentCompression()
				.setDefaultRequestConfig(RequestConfig.custom().setResponseTimeout(Timeout.ofSeconds(10))
						.setConnectionRequestTimeout(Timeout.ofSeconds(1)).build()).build();
	}

	// 응답 상태·크기 제한을 네트워크 없이 검증하기 위한 주입 지점.
	UrlFetchClient(CloseableHttpClient client) { this.client = client; }

	public byte[] fetch(URI uri) {
		HttpGet request = new HttpGet(uri);
		var deadline = deadlines.schedule(request::cancel, 20, TimeUnit.SECONDS);
		try {
			return client.execute(request, response -> {
				if (response.getCode() != 200 || response.getEntity() == null) {
					request.cancel();
					throw new BusinessException(ErrorCode.URL_IMPORT_FAILED);
				}
				if (response.getEntity().getContentLength() > LIMIT) {
					request.cancel();
					throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
				}
				try (var stream = response.getEntity().getContent()) {
					byte[] bytes = stream.readNBytes(LIMIT + 1);
					if (bytes.length > LIMIT) {
						request.cancel();
						throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
					}
					return bytes;
				}
			});
		} catch (IOException e) {
			// URL query, 원격 응답, 인증서/내부 연결 상세를 로그나 응답에 넣지 않는다.
			throw new BusinessException(ErrorCode.URL_IMPORT_FAILED);
		} finally {
			deadline.cancel(false);
		}
	}

	@PreDestroy
	public void close() throws IOException { deadlines.shutdownNow(); client.close(); }
}
