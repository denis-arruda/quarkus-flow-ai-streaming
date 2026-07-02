package dev.denisarruda.ai.boundary;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.denisarruda.content.entity.ContentEvent;
import dev.denisarruda.content.entity.Marketing;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
@SystemMessage("""
		You are a marketing copywriter specializing in content promotion.
		Based on the provided content metadata, enrichment, and sensitivity data, create:
		- headlineGlobal: a compelling global headline (max 80 characters)
		- tagline: a memorable tagline (max 40 characters)
		- shortDescription: an engaging 2-3 sentence promotional description
		Return structured data only. Do not include any explanation.
		""")
public interface MarketingAgent {
	@UserMessage("Create marketing copy for this content: {it}")
	Marketing create(@MemoryId String memoryId, ContentEvent content);
}
