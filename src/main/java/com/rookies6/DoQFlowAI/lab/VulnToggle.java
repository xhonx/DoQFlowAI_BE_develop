package com.rookies6.DoQFlowAI.lab;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

/** 시작 시 환경변수를 읽는 실습 설정. DB·변경 API·런타임 캐시를 사용하지 않는다. */
@Component
public class VulnToggle {

	private static final Set<String> KNOWN = Set.of(
			"1-1", "1-2", "1-3", "1-4", "1-5", "2-1", "2-2",
			"3-1", "3-2", "3-3", "3-4", "3-5", "3-6",
			"4-1", "4-2", "4-3", "4-4", "4-5", "5-1", "5-2",
			"6-1", "6-2", "7-1", "7-2", "7-3", "7-4", "8-1");
	private final Set<String> enabled;

	public VulnToggle(Environment environment) {
		// lab을 명시해야 하며 prod/production이 함께 켜져 있으면 무조건 off.
		if (!environment.acceptsProfiles(Profiles.of("lab & !prod & !production"))) {
			this.enabled = Set.of();
			return;
		}
		Set<String> requested = Arrays.stream(environment.getProperty("LAB_VULNS", "").split(","))
				.map(String::trim).filter(id -> !id.isEmpty()).collect(Collectors.toSet());
		if (!KNOWN.containsAll(requested)) {
			throw new IllegalArgumentException("LAB_VULNS contains an unsupported guideline ID");
		}
		this.enabled = Set.copyOf(requested);
	}

	/** 설정 변경은 프로세스/컨테이너를 다시 시작한 뒤 적용된다. */
	public boolean on(String flagId) {
		return flagId != null && enabled.contains(flagId);
	}
}
