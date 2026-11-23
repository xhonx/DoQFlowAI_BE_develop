package com.rookies6.DoQFlowAI.service;

import static org.assertj.core.api.Assertions.*;
import com.rookies6.DoQFlowAI.exception.BusinessException;
import java.net.InetAddress;
import org.junit.jupiter.api.Test;

class UrlImportPolicyTest {
	@Test void requiresExactAllowedHttpsHost() {
		UrlImportPolicy policy = new UrlImportPolicy("files.example.com");
		assertThat(policy.validate("https://files.example.com/report.txt").getHost()).isEqualTo("files.example.com");
		for (String url : new String[] {"http://files.example.com/report.txt", "file:///etc/passwd",
				"https://files.example.com.evil.test/a", "https://user:pass@files.example.com/a",
				"https://files.example.com:8443/a", "https://files.example.com/a#b", "https://127.0.0.1/a"}) {
			assertThatThrownBy(() -> policy.validate(url)).isInstanceOf(BusinessException.class);
		}
		assertThatThrownBy(() -> new UrlImportPolicy("").validate("https://files.example.com/a"))
				.isInstanceOf(BusinessException.class);
	}
	@Test void blocksInternalAndSpecialAddressesIncludingIpv6() throws Exception {
		for (String ip : new String[] {"127.0.0.1", "10.1.2.3", "172.16.1.1", "192.168.1.1", "169.254.169.254",
				"0.0.0.0", "100.64.1.1", "198.18.0.1", "224.0.0.1", "168.63.129.16", "203.0.113.1",
				"::1", "fc00::1", "fe80::1", "::ffff:127.0.0.1", "64:ff9b::7f00:1", "2001:db8::1", "2002:7f00:1::"}) {
			assertThat(UrlImportPolicy.isPublicAddress(InetAddress.getByName(ip))).as(ip).isFalse();
		}
		assertThat(UrlImportPolicy.isPublicAddress(InetAddress.getByName("8.8.8.8"))).isTrue();
		assertThat(UrlImportPolicy.isPublicAddress(InetAddress.getByName("2606:4700:4700::1111"))).isTrue();
	}
}
