package dev.denisarruda.content.control;

import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.agent;
import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.emitJson;
import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.listen;
import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.toOne;

import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.quarkiverse.flow.Flow;
import io.serverlessworkflow.api.types.Workflow;
import io.serverlessworkflow.fluent.func.FuncWorkflowBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import dev.denisarruda.ai.boundary.EnrichmentAgent;
import dev.denisarruda.ai.boundary.MarketingAgent;
import dev.denisarruda.ai.boundary.SensitivityAgent;
import dev.denisarruda.content.entity.ContentEvent;

@ApplicationScoped
public class ContentIntelligenceFlow extends Flow {

	@Inject
	EnrichmentAgent enrichmentAgent;

	@Inject
	SensitivityAgent sensitivityAgent;

	@Inject
	MarketingAgent marketingAgent;

	@Inject
	ObjectMapper objectMapper;

	@Override
	public Workflow descriptor() {
		return FuncWorkflowBuilder.workflow("content-intelligence")
				.tasks(
						listen("waitContent", toOne("dev.denisarruda.content.published"))
								.outputAs((Collection<Object> c) -> toContentEvent(c)),
						agent("enrich", enrichmentAgent::enrich, ContentEvent.class)
								.exportAs("{ enrichment: . }"),
						agent("compliance", sensitivityAgent::analyze, ContentEvent.class)
								.inputFrom(".")
								.exportAs("{ sensitivity: . }"),
						agent("marketing", marketingAgent::create, ContentEvent.class)
								.inputFrom(".")
								.exportAs("{ marketing: . }"),
						emitJson("dev.denisarruda.content.ready", Object.class))
				.build();
	}

	private ContentEvent toContentEvent(Collection<Object> events) {
		try {
			var ce = (CloudEvent) events.iterator().next();
			return objectMapper.readValue(ce.getData().toBytes(), ContentEvent.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to deserialize CloudEvent data", e);
		}
	}
}
