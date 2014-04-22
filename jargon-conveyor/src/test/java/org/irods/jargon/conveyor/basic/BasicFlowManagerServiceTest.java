package org.irods.jargon.conveyor.basic;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpecCacheService;
import org.irods.jargon.conveyor.flowmanager.flow.Selector.FlowActionEnum;
import org.irods.jargon.conveyor.flowmanager.flow.dsl.Flow;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.junit.Test;
import org.mockito.Mockito;

public class BasicFlowManagerServiceTest {

	@Test
	public void testRetrieveCandidateFlowSpecs() {
		String host = "test";
		String zone = "zone";
		FlowActionEnum action = FlowActionEnum.GET;

		String fqcn = Microservice.class.getName();

		FlowSpec flow = Flow.define().forAnyAction().forHost("blah*")
				.forZone("zone").onAllConditions().endPreOperationChain()
				.endPreFileChain().addPostFileMicroservice(fqcn)
				.endPostFileChain().endPostOperationChain()
				.endFlowWithoutErrorHandler();

		List<FlowSpec> candidates = new ArrayList<FlowSpec>();
		candidates.add(flow);

		FlowSpecCacheService flowSpecCacheService = Mockito
				.mock(FlowSpecCacheService.class);
		Mockito.when(flowSpecCacheService.getFlowSpecs())
				.thenReturn(candidates);

	}
}
