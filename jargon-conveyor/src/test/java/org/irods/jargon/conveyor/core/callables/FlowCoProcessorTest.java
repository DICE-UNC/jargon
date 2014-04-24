package org.irods.jargon.conveyor.core.callables;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.flow.FlowSpecCacheService;
import org.irods.jargon.conveyor.flowmanager.flow.Selector.FlowActionEnum;
import org.irods.jargon.conveyor.flowmanager.flow.dsl.Flow;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice.ExecResult;
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.AlwaysDontRunConditionMicroservice;
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.AlwaysRunConditionMicroservice;
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.LogAndCancelMicroservice;
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.LogAndContinueMicroservice;
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.LogAndSkipChainMicroservice;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.Test;
import org.mockito.Mockito;

public class FlowCoProcessorTest {

	@Test
	public void testEvaluateConditionNoRun() throws Exception {

		String host = "test";
		String zone = "zone";
		FlowActionEnum action = FlowActionEnum.PUT;

		String fqcn = Microservice.class.getName();
		String condFqcn = AlwaysDontRunConditionMicroservice.class.getName();

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user",
				"pwd", "", "zone", "");

		FlowSpec flow = Flow.define().forAction(action).forHost(host)
				.forZone(zone).when(condFqcn).endPreOperationChain()
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
		DefaultTransferControlBlock.instance();
		PutConveyorCallable callable = Mockito.mock(PutConveyorCallable.class);
		Mockito.when(callable.getCandidateFlowSpecs()).thenReturn(candidates);
		Mockito.when(callable.getConveyorService()).thenReturn(conveyorService);
		Mockito.when(callable.getIrodsAccessObjectFactory()).thenReturn(
				irodsAccessObjectFactory);
		Mockito.when(callable.getTransferAttempt()).thenReturn(transferAttempt);
		Mockito.when(callable.getTransfer()).thenReturn(transfer);
		Mockito.when(callable.getIRODSAccountForGridAccount(gridAccount))
				.thenReturn(irodsAccount);
		Mockito.when(callable.getTransferControlBlock()).thenReturn(
				DefaultTransferControlBlock.instance());

		FlowCoProcessor flowCoProcessor = new FlowCoProcessor(callable);
		TransferStatus status = Mockito.mock(TransferStatus.class);
		boolean actual = flowCoProcessor.evaluateCondition(flow, status);
		Assert.assertFalse("should have evaluated condition to false", actual);

	}

	@Test
	public void testEvaluateCondition() throws Exception {

		String host = "test";
		String zone = "zone";
		FlowActionEnum action = FlowActionEnum.PUT;

		String fqcn = Microservice.class.getName();
		String condFqcn = AlwaysRunConditionMicroservice.class.getName();

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user",
				"pwd", "", "zone", "");

		FlowSpec flow = Flow.define().forAction(action).forHost(host)
				.forZone(zone).when(condFqcn).endPreOperationChain()
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
		DefaultTransferControlBlock.instance();
		PutConveyorCallable callable = Mockito.mock(PutConveyorCallable.class);
		Mockito.when(callable.getCandidateFlowSpecs()).thenReturn(candidates);
		Mockito.when(callable.getConveyorService()).thenReturn(conveyorService);
		Mockito.when(callable.getIrodsAccessObjectFactory()).thenReturn(
				irodsAccessObjectFactory);
		Mockito.when(callable.getTransferAttempt()).thenReturn(transferAttempt);
		Mockito.when(callable.getTransfer()).thenReturn(transfer);
		Mockito.when(callable.getIRODSAccountForGridAccount(gridAccount))
				.thenReturn(irodsAccount);
		Mockito.when(callable.getTransferControlBlock()).thenReturn(
				DefaultTransferControlBlock.instance());

		FlowCoProcessor flowCoProcessor = new FlowCoProcessor(callable);
		TransferStatus status = Mockito.mock(TransferStatus.class);

		boolean actual = flowCoProcessor.evaluateCondition(flow, status);
		Assert.assertTrue("should have evaluated condition to true", actual);

	}

	@Test
	public void testRunPreOpChainRunTwoSkipLastOne() throws Exception {

		String host = "test";
		String zone = "zone";
		FlowActionEnum action = FlowActionEnum.PUT;

		String LogAndContinueFqcn = LogAndContinueMicroservice.class.getName();
		String LogAndSkipChainFqcn = LogAndSkipChainMicroservice.class
				.getName();
		String condFqcn = AlwaysRunConditionMicroservice.class.getName();

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user",
				"pwd", "", "zone", "");

		FlowSpec flow = Flow.define().forAction(action).forHost(host)
				.forZone(zone).when(condFqcn)
				.addPreOperationMicroservice(LogAndContinueFqcn)
				.addPreOperationMicroservice(LogAndContinueFqcn)
				.addPreOperationMicroservice(LogAndSkipChainFqcn)
				.addPreOperationMicroservice(LogAndContinueFqcn)
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
		transferAttempt.setTransfer(transfer);

		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		DefaultTransferControlBlock.instance();
		PutConveyorCallable callable = Mockito.mock(PutConveyorCallable.class);
		Mockito.when(callable.getCandidateFlowSpecs()).thenReturn(candidates);
		Mockito.when(callable.getConveyorService()).thenReturn(conveyorService);
		Mockito.when(callable.getIrodsAccessObjectFactory()).thenReturn(
				irodsAccessObjectFactory);
		Mockito.when(callable.getTransferAttempt()).thenReturn(transferAttempt);
		Mockito.when(callable.getTransfer()).thenReturn(transfer);
		Mockito.when(callable.getIRODSAccountForGridAccount(gridAccount))
				.thenReturn(irodsAccount);
		Mockito.when(callable.getTransferControlBlock()).thenReturn(
				DefaultTransferControlBlock.instance());

		FlowCoProcessor flowCoProcessor = new FlowCoProcessor(callable);
		TransferStatus status = Mockito.mock(TransferStatus.class);

		ExecResult execResult = flowCoProcessor.executePreOperationChain(flow,
				status);
		Assert.assertEquals("should have gotten skip chain",
				ExecResult.SKIP_THIS_CHAIN, execResult);

	}

	@Test
	public void testRunPreOpChainRunTwoThenCancel() throws Exception {

		String host = "test";
		String zone = "zone";
		FlowActionEnum action = FlowActionEnum.PUT;

		String logAndContinueFqcn = LogAndContinueMicroservice.class.getName();
		String logAndCancelFqcn = LogAndCancelMicroservice.class.getName();
		String condFqcn = AlwaysRunConditionMicroservice.class.getName();

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user",
				"pwd", "", "zone", "");

		FlowSpec flow = Flow.define().forAction(action).forHost(host)
				.forZone(zone).when(condFqcn)
				.addPreOperationMicroservice(logAndContinueFqcn)
				.addPreOperationMicroservice(logAndContinueFqcn)
				.addPreOperationMicroservice(logAndCancelFqcn)
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
		transferAttempt.setTransfer(transfer);

		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		DefaultTransferControlBlock.instance();
		PutConveyorCallable callable = Mockito.mock(PutConveyorCallable.class);
		Mockito.when(callable.getCandidateFlowSpecs()).thenReturn(candidates);
		Mockito.when(callable.getConveyorService()).thenReturn(conveyorService);
		Mockito.when(callable.getIrodsAccessObjectFactory()).thenReturn(
				irodsAccessObjectFactory);
		Mockito.when(callable.getTransferAttempt()).thenReturn(transferAttempt);
		Mockito.when(callable.getTransfer()).thenReturn(transfer);
		Mockito.when(callable.getIRODSAccountForGridAccount(gridAccount))
				.thenReturn(irodsAccount);
		Mockito.when(callable.getTransferControlBlock()).thenReturn(
				DefaultTransferControlBlock.instance());

		FlowCoProcessor flowCoProcessor = new FlowCoProcessor(callable);
		TransferStatus status = Mockito.mock(TransferStatus.class);

		ExecResult execResult = flowCoProcessor.executePreOperationChain(flow,
				status);
		Assert.assertEquals("should have gotten cancel",
				ExecResult.CANCEL_OPERATION, execResult);
		Assert.assertTrue("tcb should be set to cancel", callable
				.getTransferControlBlock().isCancelled());

	}

	@Test
	public void testRunPreFileChain() throws Exception {

		String host = "test";
		String zone = "zone";
		FlowActionEnum action = FlowActionEnum.PUT;

		String LogAndContinueFqcn = LogAndContinueMicroservice.class.getName();
		String LogAndSkipChainFqcn = LogAndSkipChainMicroservice.class
				.getName();
		String condFqcn = AlwaysRunConditionMicroservice.class.getName();

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user",
				"pwd", "", "zone", "");

		FlowSpec flow = Flow.define().forAction(action).forHost(host)
				.forZone(zone).when(condFqcn).endPreOperationChain()
				.addPreFileMicroservice(LogAndContinueFqcn)
				.addPreFileMicroservice(LogAndContinueFqcn)
				.addPreFileMicroservice(LogAndSkipChainFqcn)
				.addPreFileMicroservice(LogAndContinueFqcn).endPreFileChain()
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
		DefaultTransferControlBlock.instance();
		PutConveyorCallable callable = Mockito.mock(PutConveyorCallable.class);
		Mockito.when(callable.getCandidateFlowSpecs()).thenReturn(candidates);
		Mockito.when(callable.getConveyorService()).thenReturn(conveyorService);
		Mockito.when(callable.getIrodsAccessObjectFactory()).thenReturn(
				irodsAccessObjectFactory);
		Mockito.when(callable.getTransferAttempt()).thenReturn(transferAttempt);
		Mockito.when(callable.getTransfer()).thenReturn(transfer);
		Mockito.when(callable.getIRODSAccountForGridAccount(gridAccount))
				.thenReturn(irodsAccount);
		Mockito.when(callable.getTransferControlBlock()).thenReturn(
				DefaultTransferControlBlock.instance());

		FlowCoProcessor flowCoProcessor = new FlowCoProcessor(callable);
		TransferStatus status = Mockito.mock(TransferStatus.class);

		ExecResult execResult = flowCoProcessor.executePreFileChain(flow,
				status);
		Assert.assertEquals("should have gotten skip chain",
				ExecResult.SKIP_THIS_CHAIN, execResult);

		Object countObjVal = flowCoProcessor.getInvocationContext()
				.getSharedProperties()
				.get(LogAndContinueMicroservice.COUNT_KEY);
		Assert.assertNotNull("did not get count key in invocation context");

		int count = (Integer) countObjVal;

		Assert.assertEquals("did not count two microservice calls", 2, count);

	}

	@Test
	public void testRunPostFileChain() throws Exception {

		String host = "test";
		String zone = "zone";
		FlowActionEnum action = FlowActionEnum.PUT;

		String LogAndContinueFqcn = LogAndContinueMicroservice.class.getName();

		String condFqcn = AlwaysRunConditionMicroservice.class.getName();

		IRODSAccessObjectFactory irodsAccessObjectFactory = Mockito
				.mock(IRODSAccessObjectFactory.class);

		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user",
				"pwd", "", "zone", "");

		FlowSpec flow = Flow.define().forAction(action).forHost(host)
				.forZone(zone).when(condFqcn).endPreOperationChain()
				.endPreFileChain().addPostFileMicroservice(LogAndContinueFqcn)
				.addPostFileMicroservice(LogAndContinueFqcn).endPostFileChain()
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
		transferAttempt.setTransfer(transfer);

		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		DefaultTransferControlBlock.instance();
		PutConveyorCallable callable = Mockito.mock(PutConveyorCallable.class);
		Mockito.when(callable.getCandidateFlowSpecs()).thenReturn(candidates);
		Mockito.when(callable.getConveyorService()).thenReturn(conveyorService);
		Mockito.when(callable.getIrodsAccessObjectFactory()).thenReturn(
				irodsAccessObjectFactory);
		Mockito.when(callable.getTransferAttempt()).thenReturn(transferAttempt);
		Mockito.when(callable.getTransfer()).thenReturn(transfer);
		Mockito.when(callable.getIRODSAccountForGridAccount(gridAccount))
				.thenReturn(irodsAccount);
		Mockito.when(callable.getTransferControlBlock()).thenReturn(
				DefaultTransferControlBlock.instance());

		FlowCoProcessor flowCoProcessor = new FlowCoProcessor(callable);
		TransferStatus status = Mockito.mock(TransferStatus.class);

		flowCoProcessor.executePostFileChain(flow, status);

		Object countObjVal = flowCoProcessor.getInvocationContext()
				.getSharedProperties()
				.get(LogAndContinueMicroservice.COUNT_KEY);
		Assert.assertNotNull("did not get count key in invocation context");

		int count = (Integer) countObjVal;

		Assert.assertEquals("did not count two microservice calls", 2, count);

	}
}
