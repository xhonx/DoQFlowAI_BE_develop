package com.rookies6.DoQFlowAI.lab;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 취약점 토글 (인시큐어 코딩 실습용) < Final PJT >
 *
 * <p>각 취약 분기는 {@code if (vulnToggle.on("x-y")) { ...취약... } else { ...안전... }} 로 감싼다.
 * 안전 코드와 취약 코드가 둘 다 코드에 남고, 런타임에 전환된다.
 *
 * <p><b>lab 프로필에서만 동작한다.</b> app.lab.enabled=true(application-lab.yaml)일 때만
 * 플래그를 읽고, 그 외(기본·prod)에서는 항상 false를 돌려준다. 즉 배포본은 전환 불가·안전 고정이다.
 *
 * <p>상태는 vuln_flags 테이블에 저장하고 메모리에 캐시한다.
 */
@Component
public class VulnToggle {

	private final boolean labEnabled;
	private final JdbcTemplate jdbc;
	private final Map<String, Boolean> cache = new ConcurrentHashMap<>();
	private volatile boolean loaded = false;

	public VulnToggle(@Value("${app.lab.enabled:false}") boolean labEnabled, JdbcTemplate jdbc) {
		this.labEnabled = labEnabled;
		this.jdbc = jdbc;
	}

	/** 취약 분기를 켤지 여부. prod(labEnabled=false)에서는 무조건 false */
	public boolean on(String flagId) {
		if (!labEnabled) {
			return false;
		}
		ensureLoaded();
		return Boolean.TRUE.equals(cache.get(flagId));
	}

	/** 플래그 전체 상태 (컨트롤 페이지용) */
	public Map<String, Boolean> all() {
		if (!labEnabled) {
			return Map.of();
		}
		ensureLoaded();
		return Map.copyOf(cache);
	}

	/** 플래그 on/off. lab에서만 반영된다 */
	public void set(String flagId, boolean enabled) {
		if (!labEnabled) {
			return;
		}
		ensureLoaded();
		int updated = jdbc.update(
				"UPDATE vuln_flags SET enabled = ?, updated_at = ? WHERE flag_id = ?",
				enabled, LocalDateTime.now(), flagId);
		if (updated > 0) {
			cache.put(flagId, enabled);
		}
	}

	/** 최초 접근 시 1회 로드. Flyway 마이그레이션 이후 시점이라 테이블이 보장된다 */
	private void ensureLoaded() {
		if (loaded) {
			return;
		}
		synchronized (this) {
			if (loaded) {
				return;
			}
			jdbc.query("SELECT flag_id, enabled FROM vuln_flags",
					rs -> cache.put(rs.getString("flag_id"), rs.getBoolean("enabled")));
			loaded = true;
		}
	}
}
