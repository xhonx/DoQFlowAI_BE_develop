package com.rookies6.DoQFlowAI.service;

import com.rookies6.DoQFlowAI.entity.RankLevel;
import com.rookies6.DoQFlowAI.exception.BusinessException;
import com.rookies6.DoQFlowAI.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 사원/팀장 2단계 유지. 요청의 직급만으로 팀장 계정을 만들지 못하게 한다. */
@Component
public class SignupKeyPolicy {
	private final byte[] leadKey;
	public SignupKeyPolicy(@Value("${app.signup.lead-key:}") String configuredKey) {
		leadKey = configuredKey.isBlank() ? new byte[0] : configuredKey.getBytes(StandardCharsets.UTF_8);
	}
	public void validate(int rankLevel, String suppliedKey) {
		if (rankLevel != RankLevel.LEAD.level()) return;
		if (leadKey.length == 0 || suppliedKey == null
				|| !MessageDigest.isEqual(leadKey, suppliedKey.getBytes(StandardCharsets.UTF_8))) {
			throw new BusinessException(ErrorCode.INVALID_SIGNUP_KEY);
		}
	}
}
