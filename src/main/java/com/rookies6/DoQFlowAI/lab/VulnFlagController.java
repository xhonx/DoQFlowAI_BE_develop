package com.rookies6.DoQFlowAI.lab;

import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 취약점 토글 컨트롤 API (실습용). <b>lab 프로필에서만</b> 빈으로 등록된다(prod에는 존재하지 않음).
 * 관리자 컨트롤 페이지(3-4)가 이 API로 플래그를 조회·전환한다.
 */
@RestController
@RequestMapping("/admin/vuln-flags")
@Profile("lab")
public class VulnFlagController {

	private final VulnToggle vulnToggle;

	public VulnFlagController(VulnToggle vulnToggle) {
		this.vulnToggle = vulnToggle;
	}

	/** 플래그 전체 조회 */
	@GetMapping
	public Map<String, Boolean> list() {
		return vulnToggle.all();
	}

	/** 플래그 하나 on/off */
	@PatchMapping("/{flagId}")
	public Map<String, Boolean> set(@PathVariable String flagId, @RequestBody FlagUpdate body) {
		vulnToggle.set(flagId, body.enabled());
		return vulnToggle.all();
	}

	public record FlagUpdate(boolean enabled) {
	}
}
