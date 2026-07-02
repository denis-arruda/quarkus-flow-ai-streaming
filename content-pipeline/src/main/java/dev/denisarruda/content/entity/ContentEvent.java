package dev.denisarruda.content.entity;

public record ContentEvent(
		String contentId,
		String title,
		String description,
		String genre,
		String region,
		String timestamp) {
}
