package dev.denisarruda.content.entity;

import dev.langchain4j.model.output.structured.Description;
import java.util.List;

public record ContentEnrichment(
		@Description("3-5 thematic categories") List<String> themes,
		@Description("Dominant emotional tone, e.g. Inspiring, Dramatic, Comedic, Suspenseful") String emotionalTone,
		@Description("Brief description of the target audience") String audienceProfile,
		@Description("5-8 relevant keywords for searchability") List<String> keywords) {
}
