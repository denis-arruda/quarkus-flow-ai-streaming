package dev.denisarruda.frontweb.content.entity;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public record ContentPublishedEvent(String contentId, String title, String description,
		String genre, String region, String timestamp) {

	public JsonObject toJSON() {
		var data = Json.createObjectBuilder().add("contentId", contentId).add("title", title)
				.add("description", description).add("genre", genre).add("region", region)
				.add("timestamp", timestamp).build();
		return Json.createObjectBuilder()
				.add("specversion", "1.0")
				.add("type", "dev.denisarruda.content.published")
				.add("source", "/frontend-web")
				.add("id", contentId)
				.add("time", timestamp)
				.add("datacontenttype", "application/json")
				.add("data", data)
				.build();
	}
}
