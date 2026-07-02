package dev.denisarruda.frontweb.content.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContentPublishedEventTest {

	@Test
	void serialisesToJSON() {
		var event = new ContentPublishedEvent("c-001", "Inception",
				"A thief who steals corporate secrets through dream-sharing technology.", "Sci-Fi",
				"GLOBAL", "2026-03-01T10:00:00Z");

		var json = event.toJSON();

		assertThat(json.getString("specversion")).isEqualTo("1.0");
		assertThat(json.getString("type")).isEqualTo("dev.denisarruda.content.published");
		assertThat(json.getString("id")).isEqualTo("c-001");
		var data = json.getJsonObject("data");
		assertThat(data.getString("contentId")).isEqualTo("c-001");
		assertThat(data.getString("title")).isEqualTo("Inception");
		assertThat(data.getString("genre")).isEqualTo("Sci-Fi");
	}

	@Test
	void serialisesAllFields() {
		var event = new ContentPublishedEvent("id", "title", "desc", "Drama", "DE",
				"2026-01-01T00:00:00Z");

		var json = event.toJSON();

		var data = json.getJsonObject("data");
		assertThat(data.getString("description")).isEqualTo("desc");
		assertThat(data.getString("region")).isEqualTo("DE");
		assertThat(data.getString("timestamp")).isEqualTo("2026-01-01T00:00:00Z");
	}
}
