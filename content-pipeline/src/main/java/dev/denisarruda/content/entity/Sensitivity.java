package dev.denisarruda.content.entity;

import dev.langchain4j.model.output.structured.Description;
import java.util.List;

public record Sensitivity(
		@Description("Appropriate age rating, e.g. G, PG, PG-13, R") String ageRatingSuggested,
		@Description("ISO 3166-1 alpha-2 region codes where content may be restricted") List<String> sensitiveRegions,
		@Description("Sensitivity concerns, e.g. Violence, Language, AdultThemes, PoliticalContent") List<String> riskFlags) {
}
