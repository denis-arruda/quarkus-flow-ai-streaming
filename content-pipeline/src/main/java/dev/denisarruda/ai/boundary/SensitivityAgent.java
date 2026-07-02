package dev.denisarruda.ai.boundary;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.denisarruda.content.entity.ContentEvent;
import dev.denisarruda.content.entity.Sensitivity;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
@SystemMessage("""
		You are a content compliance and sensitivity analyst.
		Evaluate the provided content metadata and enrichment to determine:
		- ageRatingSuggested: appropriate age rating (e.g. G, PG, PG-13, R)
		- sensitiveRegions: list of regions where content may be restricted or require review
		- riskFlags: list of sensitivity concerns (e.g. Violence, Language, AdultThemes, PoliticalContent)
		Return structured data only. Do not include any explanation.
		""")
public interface SensitivityAgent {
	@UserMessage("Evaluate sensitivity and compliance for this content: {it}")
	Sensitivity analyze(@MemoryId String memoryId, ContentEvent content);
}
