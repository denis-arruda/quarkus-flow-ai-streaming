package dev.denisarruda.content.entity;

import dev.langchain4j.model.output.structured.Description;

public record Marketing(
		@Description("Compelling global headline, max 80 characters") String headlineGlobal,
		@Description("Memorable tagline, max 40 characters") String tagline,
		@Description("Engaging 2-3 sentence promotional description") String shortDescription) {
}
