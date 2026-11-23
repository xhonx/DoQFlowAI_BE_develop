package com.rookies6.DoQFlowAI.lab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class VulnToggleTest {
	@Test
	void defaultsOffAndRequiresExplicitLab() {
		assertThat(new VulnToggle(new MockEnvironment()).on("1-1")).isFalse();
		assertThat(new VulnToggle(new MockEnvironment().withProperty("LAB_VULNS", "1-1")).on("1-1"))
				.isFalse();
	}

	@Test
	void loadsOnceWithoutDatabase() {
		MockEnvironment env = new MockEnvironment().withProperty("LAB_VULNS", " 1-1, 2-2,1-1, ");
		env.setActiveProfiles("lab");
		VulnToggle toggle = new VulnToggle(env);
		assertThat(toggle.on("1-1")).isTrue();
		assertThat(toggle.on("2-2")).isTrue();
		assertThat(toggle.on("4-2")).isFalse();
		assertThat(toggle.on(null)).isFalse();
		env.setProperty("LAB_VULNS", "4-2");
		assertThat(toggle.on("4-2")).isFalse();
	}

	@Test
	void productionWinsEvenWhenLabIsAlsoActive() {
		for (String profile : new String[] {"prod", "production"}) {
			MockEnvironment env = new MockEnvironment().withProperty("LAB_VULNS", "1-1");
			env.setActiveProfiles("lab", profile);
			assertThat(new VulnToggle(env).on("1-1")).isFalse();
		}
	}

	@Test
	void rejectsTyposInsteadOfSilentlySkippingScenarios() {
		MockEnvironment env = new MockEnvironment().withProperty("LAB_VULNS", "1-6");
		env.setActiveProfiles("lab");
		assertThatThrownBy(() -> new VulnToggle(env)).isInstanceOf(IllegalArgumentException.class);
	}
}
