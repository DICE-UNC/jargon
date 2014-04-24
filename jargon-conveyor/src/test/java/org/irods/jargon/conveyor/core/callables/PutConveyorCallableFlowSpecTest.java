package org.irods.jargon.conveyor.core.callables;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.irods.jargon.conveyor.basic.BasicFlowManagerService;
import org.irods.jargon.conveyor.core.ConfigurationService;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpecCacheService;
import org.irods.jargon.conveyor.flowmanager.flow.Selector.FlowActionEnum;
import org.irods.jargon.conveyor.flowmanager.flow.dsl.Flow;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.AlwaysRunConditionMicroservice;
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.LogAndContinueMicroservice;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
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

	@Test
	public void testOverallStatusCallbackTriggerPreOpChain() throws Exception {

		String host = "test";
		String zone = "zone";
		FlowActionEnum action = FlowActionEnum.PUT;

		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user",
				"pwd", "", "zone", "");

		String logAndContinueFqcn = LogAndContinueMicroservice.class.getName();
		String condFqcn = AlwaysRunConditionMicroservice.class.getName();

		FlowSpec flow = Flow.define().forAction(action).forHost(host)
				.forZone(zone).when(condFqcn)
				.addPreOperationMicroservice(logAndContinueFqcn)
				.addPreOperationMicroservice(logAndContinueFqcn)
				.endPreOperationChain().endPreFileChain().endPostFileChain()
				.endPostOperationChain().endFlowWithoutErrorHandler();

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
		transferAttempt.setLastSuccessfulPath("");
		transferAttempt.setTransfer(transfer);

		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);
		Mockito.when(conveyorService.getIrodsAccessObjectFactory()).thenReturn(
				irodsAccessObjectFactory);

		DataTransferOperations dto = Mockito.mock(DataTransferOperations.class);
		Mockito.when(
				irodsAccessObjectFactory
						.getDataTransferOperations(irodsAccount)).thenReturn(
				dto);

		BasicFlowManagerService flowManagerService = Mockito
				.mock(BasicFlowManagerService.class);

		TransferControlBlock transferControlBlock = DefaultTransferControlBlock
				.instance();

		Mockito.when(
				flowManagerService.retrieveCandidateFlowSpecs(transferAttempt))
				.thenReturn(candidates);
		Mockito.when(conveyorService.getFlowManagerService()).thenReturn(
				flowManagerService);

		GridAccountService gridAccountService = Mockito
				.mock(GridAccountService.class);
		Mockito.when(gridAccountService.irodsAccountForGridAccount(gridAccount))
				.thenReturn(irodsAccount);
		Mockito.when(conveyorService.getGridAccountService()).thenReturn(
				gridAccountService);

		ConfigurationService configurationService = Mockito
				.mock(ConfigurationService.class);
		Mockito.when(
				configurationService
						.buildDefaultTransferControlBlockBasedOnConfiguration(
								"", irodsAccessObjectFactory)).thenReturn(
				transferControlBlock);
		Mockito.when(conveyorService.getConfigurationService()).thenReturn(
				configurationService);

		PutConveyorCallable callable = new PutConveyorCallable(transferAttempt,
				conveyorService);

		TransferStatus status = TransferStatus.instance(
				TransferStatus.TransferType.PUT, "x", "x", "x", 1L, 1L, 1, 1,
				1, TransferState.OVERALL_INITIATION, gridAccount.getHost(),
				gridAccount.getZone());

		callable.setTransferControlBlock(transferControlBlock);
		callable.call();
		callable.overallStatusCallback(status);

		Object countObjVal = callable.getFlowCoProcessor()
				.getInvocationContext().getSharedProperties()
				.get(LogAndContinueMicroservice.COUNT_KEY);
		Assert.assertNotNull("did not get count key in invocation context");

		int count = (Integer) countObjVal;
		Assert.assertEquals("did not count two microservice calls", 2, count);

	}
}
