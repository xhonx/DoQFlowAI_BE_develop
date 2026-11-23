package com.rookies6.DoQFlowAI.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 < API 4.1 >
 *
 * <p>rankLevel을 요청에서 받는다. 사원(1)·팀장(2) 2단계다.
 *
 * <p>팀장 가입 키는 AuthService의 SignupKeyPolicy가 서버에서 검증한다.
 * 별도 ADMIN 역할은 없으며 사원(1)·팀장(2) 체계를 유지한다.
 */
public record SignupRequest(

		@NotBlank
		@Email
		@Size(max = 255)
		String email,

		/** 영문·숫자·특수문자 중 2종 이상 < API 2.6 > */
		@NotBlank
		@Size(min = 8, max = 64)
		@Pattern(
				regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$|^(?=.*[A-Za-z])(?=.*[^A-Za-z\\d]).*$|^(?=.*\\d)(?=.*[^A-Za-z\\d]).*$",
				message = "영문·숫자·특수문자 중 2종 이상을 포함해야 합니다.")
		String password,

		@NotBlank
		@Size(min = 1, max = 50)
		String name,

		@Min(1)
		@Max(2)
		int rankLevel,

		/** rankLevel=2일 때 필수. 서버에서 설정값과 대조한다 */
		String signupKey
) {
}
