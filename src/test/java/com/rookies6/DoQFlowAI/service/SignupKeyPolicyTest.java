package com.rookies6.DoQFlowAI.service;

import static org.assertj.core.api.Assertions.*;
import com.rookies6.DoQFlowAI.exception.BusinessException;
import org.junit.jupiter.api.Test;

class SignupKeyPolicyTest {
	@Test void staffDoesNotNeedKey() { new SignupKeyPolicy("").validate(1, null); }
	@Test void leadRequiresConfiguredExactKey() {
		SignupKeyPolicy policy = new SignupKeyPolicy("test-lead-key");
		policy.validate(2, "test-lead-key");
		for (String key : new String[] {null, "", "wrong", " test-lead-key"}) {
			assertThatThrownBy(() -> policy.validate(2, key)).isInstanceOf(BusinessException.class);
		}
	}
	@Test void missingConfigurationCannotGrantLead() {
		assertThatThrownBy(() -> new SignupKeyPolicy("").validate(2, ""))
				.isInstanceOf(BusinessException.class);
	}
}
