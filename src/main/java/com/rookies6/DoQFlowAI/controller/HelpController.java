package com.rookies6.DoQFlowAI.controller;

import com.rookies6.DoQFlowAI.dto.response.ApiResponse;
import com.rookies6.DoQFlowAI.exception.BusinessException;
import com.rookies6.DoQFlowAI.exception.ErrorCode;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 사용자 입력을 파일 경로로 결합하지 않고 정해진 안내만 반환한다. 인증 필요. */
@RestController
public class HelpController {
	private static final Map<String, HelpPage> PAGES = Map.of(
			"guide", new HelpPage("사용 안내", "팀장은 문서를 만들고 버전을 추가하며 열람자를 초대합니다. 사원은 초대를 수락한 문서를 열람합니다."),
			"terms", new HelpPage("실습 이용 안내", "이 환경은 팀의 로컬 개발·진단 실습용입니다. 실제 회사 자료나 타인의 개인정보를 올리지 마세요."),
			"privacy", new HelpPage("실습 데이터 안내", "실습에는 가상 계정과 샘플 문서를 사용합니다. 이 안내는 운영 서비스의 법적 개인정보 처리방침을 대신하지 않습니다."));

	@GetMapping("/help")
	public ApiResponse<HelpPage> get(@RequestParam(defaultValue = "guide") String page) {
		HelpPage result = PAGES.get(page);
		if (result == null) throw new BusinessException(ErrorCode.NOT_FOUND);
		return ApiResponse.ok(result);
	}

	public record HelpPage(String title, String content) { }
}
