package dev.denisarruda.ai.boundary;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.denisarruda.content.entity.ContentEnrichment;
import dev.denisarruda.content.entity.ContentEvent;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
@SystemMessage("""
		You are a content enrichment specialist. Analyze the provided content metadata and extract:
		- themes: a list of 3-5 thematic categories
		- emotionalTone: the dominant emotional tone (e.g. Inspiring, Dramatic, Comedic, Suspenseful)
		- audienceProfile: a brief description of the target audience
		- keywords: a list of 5-8 relevant keywords for searchability
		Return structured data only. Do not include any explanation.
		""")
public interface EnrichmentAgent {
	@UserMessage("Analyze this content and return enrichment data: {it}")
	ContentEnrichment enrich(@MemoryId String memoryId, ContentEvent content);
}
