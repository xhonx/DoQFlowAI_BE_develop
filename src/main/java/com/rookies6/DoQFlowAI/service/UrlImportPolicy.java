package com.rookies6.DoQFlowAI.service;

import com.rookies6.DoQFlowAI.exception.BusinessException;
import com.rookies6.DoQFlowAI.exception.ErrorCode;
import java.net.InetAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** URL의 허용 출처와 실제 연결 주소를 별도로 검증한다. */
@Component
public class UrlImportPolicy {
	private final Set<String> allowedHosts;
	public UrlImportPolicy(@Value("${URL_IMPORT_ALLOWED_HOSTS:}") String hosts) {
		allowedHosts = Arrays.stream(hosts.split(",")).map(String::trim)
				.map(host -> host.toLowerCase(Locale.ROOT)).filter(host -> !host.isEmpty()).collect(Collectors.toSet());
	}

	public URI validate(String value) {
		try {
			if (value == null || value.length() > 2048) throw new IllegalArgumentException();
			URI uri = URI.create(value);
			String host = uri.getHost();
			if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null
					|| host.matches("[0-9.]+") || host.contains(":")
					|| !allowedHosts.contains(host.toLowerCase(Locale.ROOT))
					|| uri.getUserInfo() != null || uri.getFragment() != null
					|| (uri.getPort() != -1 && uri.getPort() != 443)) throw new IllegalArgumentException();
			return uri;
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.VALIDATION_ERROR, "URL import requires an allowed HTTPS host on port 443");
		}
	}

	/** 연결 직전 DNS 결과에 적용한다. 모든 주소가 공인 주소여야 한다. */
	static boolean isPublicAddress(InetAddress address) {
		if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
				|| address.isSiteLocalAddress() || address.isMulticastAddress()) return false;
		byte[] raw = address.getAddress();
		int a = raw[0] & 255, b = raw[1] & 255;
		if (raw.length == 4) {
			int c = raw[2] & 255;
			return a != 0 && a != 10 && a != 127 && a < 224
					&& !(a == 100 && b >= 64 && b <= 127)
					&& !(a == 169 && b == 254) && !(a == 172 && b >= 16 && b <= 31)
					&& !(a == 192 && (b == 168 || b == 0 || (b == 88 && c == 99)))
					&& !(a == 198 && (b == 18 || b == 19 || (b == 51 && c == 100)))
					&& !(a == 203 && b == 0 && c == 113)
					&& !address.getHostAddress().equals("168.63.129.16");
		}
		// IPv6는 global unicast만 허용하고 전환/문서/특수 대역은 차단한다.
		return raw.length == 16 && (a & 0xe0) == 0x20
				&& !(a == 0x20 && b == 0x02)
				&& !(a == 0x20 && b == 0x01 && (raw[2] & 255) < 2)
				&& !(a == 0x20 && b == 0x01 && (raw[2] & 255) == 0x0d && (raw[3] & 255) == 0xb8)
				&& !(a == 0x3f && (b & 0xf0) == 0xf0);
	}
}
