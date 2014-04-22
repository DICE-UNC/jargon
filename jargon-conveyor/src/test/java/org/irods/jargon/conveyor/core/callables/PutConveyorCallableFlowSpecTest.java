package org.irods.jargon.conveyor.core.callables;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.irods.jargon.conveyor.basic.BasicFlowManagerService;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpecCacheService;
import org.irods.jargon.conveyor.flowmanager.flow.Selector.FlowActionEnum;
import org.irods.jargon.conveyor.flowmanager.flow.dsl.Flow;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.Test;
import org.mockito.Mockito;

public class PutConveyorCallableFlowSpecTest {

	@Test
	public void testRetrieveCandidateFlowSpecs() throws Exception {

		String host = "test";
		String zone = "zone";
		FlowActionEnum action = FlowActionEnum.PUT;

		String fqcn = Microservice.class.getName();

		FlowSpec flow = Flow.define().forAction(action).forHost(host)
				.forZone(zone).onAllConditions().endPreOperationChain()
				.endPreFileChain().addPostFileMicroservice(fqcn)
				.endPostFileChain().endPostOperationChain()
				.endFlowWithoutErrorHandler();

		List<FlowSpec> candidates = new ArrayList<FlowSpec>();
		candidates.add(flow);

		FlowSpecCacheService flowSpecCacheService = Mockito
				.mock(FlowSpecCacheService.class);
		Mockito.when(flowSpecCacheService.getFlowSpecs())
				.thenReturn(candidates);

		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setHost(host);
		gridAccount.setZone(zone);
		transfer.setTransferType(TransferType.PUT);
		transfer.setGridAccount(gridAccount);
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);

		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		BasicFlowManagerService flowManagerService = Mockito
				.mock(BasicFlowManagerService.class);

		Mockito.when(
				flowManagerService.retrieveCandidateFlowSpecs(transferAttempt))
				.thenReturn(candidates);
		Mockito.when(conveyorService.getFlowManagerService()).thenReturn(
				flowManagerService);

		PutConveyorCallable callable = new PutConveyorCallable(transferAttempt,
				conveyorService);

		List<FlowSpec> flowSpecs = callable.getCandidateFlowSpecs();
		Assert.assertEquals("did not get the flow spec as a candidate", 1,
				flowSpecs.size());

	}

}
