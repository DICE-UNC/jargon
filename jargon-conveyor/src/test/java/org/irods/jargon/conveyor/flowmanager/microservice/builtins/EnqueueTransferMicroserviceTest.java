package org.irods.jargon.conveyor.flowmanager.microservice.builtins;

import junit.framework.Assert;

import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.core.QueueManagerService;
import org.irods.jargon.conveyor.flowmanager.microservice.ContainerEnvironment;
import org.irods.jargon.conveyor.flowmanager.microservice.InvocationContext;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice.ExecResult;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.transfer.DefaultTransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferState;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.Test;
import org.mockito.Mockito;

public class EnqueueTransferMicroserviceTest {

	@Test
	public void testEnqueueTransfer() throws Exception {

		String host = "test";
		String zone = "zone";
		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setHost(host);
		gridAccount.setZone(zone);
		transfer.setTransferType(TransferType.PUT);
		transfer.setGridAccount(gridAccount);
		transfer.setLocalAbsolutePath("local");
		transfer.setIrodsAbsolutePath("irods");
		transfer.setResourceName("resource");
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);
		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user",
				"password", "", "zone", "");

		TransferStatus transferStatus = TransferStatus.instance(
				TransferStatus.TransferType.PUT, "zoop", "blah", "", 0, 0, 0,
				0, 0, TransferState.OVERALL_INITIATION, "host", "zone");

		DefaultTransferControlBlock.instance();

		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		QueueManagerService queueManagerService = Mockito
				.mock(QueueManagerService.class);
		Mockito.when(conveyorService.getQueueManagerService()).thenReturn(
				queueManagerService);

		Microservice enqueueTransferMicroservice = new EnqueueTransferMicroservice();
		InvocationContext invocationContext = new InvocationContext();
		ContainerEnvironment containerEnvironment = new ContainerEnvironment();
		containerEnvironment.setConveyorService(conveyorService);

		invocationContext.setTransferAttempt(transferAttempt);
		invocationContext.setIrodsAccount(irodsAccount);

		enqueueTransferMicroservice.setInvocationContext(invocationContext);
		enqueueTransferMicroservice
				.setContainerEnvironment(containerEnvironment);
		ExecResult result = enqueueTransferMicroservice.execute(transferStatus);
		Assert.assertEquals("should get continue as exec result",
				ExecResult.CONTINUE, result);

		Transfer updatedTransfer = (Transfer) invocationContext
				.getSharedProperties().get(
						EnqueueTransferMicroservice.ENQUEUED_TRANSFER);

		Assert.assertNotNull("no transfer in shared context", updatedTransfer);
		Assert.assertEquals("local path not found",
				transfer.getLocalAbsolutePath(),
				updatedTransfer.getLocalAbsolutePath());
		Assert.assertEquals("irods path not found",
				transfer.getIrodsAbsolutePath(),
				updatedTransfer.getIrodsAbsolutePath());
		Assert.assertEquals("resource not found", transfer.getResourceName(),
				updatedTransfer.getResourceName());

	}

	@Test
	public void testEnqueueTransferOverrideLocal() throws Exception {

		String host = "test";
		String zone = "zone";
		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setHost(host);
		gridAccount.setZone(zone);
		transfer.setTransferType(TransferType.PUT);
		transfer.setGridAccount(gridAccount);
		transfer.setLocalAbsolutePath("local");
		transfer.setIrodsAbsolutePath("irods");
		transfer.setResourceName("resource");
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);
		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user",
				"password", "", "zone", "");

		TransferStatus transferStatus = TransferStatus.instance(
				TransferStatus.TransferType.PUT, "zoop", "blah", "", 0, 0, 0,
				0, 0, TransferState.OVERALL_INITIATION, "host", "zone");

		DefaultTransferControlBlock.instance();

		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		QueueManagerService queueManagerService = Mockito
				.mock(QueueManagerService.class);
		Mockito.when(conveyorService.getQueueManagerService()).thenReturn(
				queueManagerService);

		Microservice enqueueTransferMicroservice = new EnqueueTransferMicroservice();
		InvocationContext invocationContext = new InvocationContext();
		ContainerEnvironment containerEnvironment = new ContainerEnvironment();
		containerEnvironment.setConveyorService(conveyorService);

		invocationContext.setTransferAttempt(transferAttempt);
		invocationContext.setIrodsAccount(irodsAccount);

		invocationContext.getSharedProperties().put(
				EnqueueTransferMicroservice.LOCAL_FILE_NAME, "boo");

		enqueueTransferMicroservice.setInvocationContext(invocationContext);
		enqueueTransferMicroservice
				.setContainerEnvironment(containerEnvironment);
		ExecResult result = enqueueTransferMicroservice.execute(transferStatus);
		Assert.assertEquals("should get continue as exec result",
				ExecResult.CONTINUE, result);

		Transfer updatedTransfer = (Transfer) invocationContext
				.getSharedProperties().get(
						EnqueueTransferMicroservice.ENQUEUED_TRANSFER);

		Assert.assertNotNull("no transfer in shared context", updatedTransfer);
		Assert.assertEquals("local path not overridden", "boo",
				updatedTransfer.getLocalAbsolutePath());
		Assert.assertEquals("irods path not found",
				transfer.getIrodsAbsolutePath(),
				updatedTransfer.getIrodsAbsolutePath());
		Assert.assertEquals("resource not found", transfer.getResourceName(),
				updatedTransfer.getResourceName());

	}

	@Test
	public void testEnqueueTransferOverrideIrods() throws Exception {

		String host = "test";
		String zone = "zone";
		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setHost(host);
		gridAccount.setZone(zone);
		transfer.setTransferType(TransferType.PUT);
		transfer.setGridAccount(gridAccount);
		transfer.setLocalAbsolutePath("local");
		transfer.setIrodsAbsolutePath("irods");
		transfer.setResourceName("resource");
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);
		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user",
				"password", "", "zone", "");

		TransferStatus transferStatus = TransferStatus.instance(
				TransferStatus.TransferType.PUT, "zoop", "blah", "", 0, 0, 0,
				0, 0, TransferState.OVERALL_INITIATION, "host", "zone");

		DefaultTransferControlBlock.instance();

		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		QueueManagerService queueManagerService = Mockito
				.mock(QueueManagerService.class);
		Mockito.when(conveyorService.getQueueManagerService()).thenReturn(
				queueManagerService);

		Microservice enqueueTransferMicroservice = new EnqueueTransferMicroservice();
		InvocationContext invocationContext = new InvocationContext();
		ContainerEnvironment containerEnvironment = new ContainerEnvironment();
		containerEnvironment.setConveyorService(conveyorService);

		invocationContext.setTransferAttempt(transferAttempt);
		invocationContext.setIrodsAccount(irodsAccount);

		invocationContext.getSharedProperties().put(
				EnqueueTransferMicroservice.IRODS_FILE_NAME, "boo");

		enqueueTransferMicroservice.setInvocationContext(invocationContext);
		enqueueTransferMicroservice
				.setContainerEnvironment(containerEnvironment);
		ExecResult result = enqueueTransferMicroservice.execute(transferStatus);
		Assert.assertEquals("should get continue as exec result",
				ExecResult.CONTINUE, result);

		Transfer updatedTransfer = (Transfer) invocationContext
				.getSharedProperties().get(
						EnqueueTransferMicroservice.ENQUEUED_TRANSFER);

		Assert.assertNotNull("no transfer in shared context", updatedTransfer);

		Assert.assertEquals("irods path not updated", "boo",
				updatedTransfer.getIrodsAbsolutePath());

	}

	@Test
	public void testEnqueueTransferOverrideResource() throws Exception {

		String host = "test";
		String zone = "zone";
		Transfer transfer = new Transfer();
		GridAccount gridAccount = new GridAccount();
		gridAccount.setHost(host);
		gridAccount.setZone(zone);
		transfer.setTransferType(TransferType.PUT);
		transfer.setGridAccount(gridAccount);
		transfer.setLocalAbsolutePath("local");
		transfer.setIrodsAbsolutePath("irods");
		transfer.setResourceName("resource");
		TransferAttempt transferAttempt = new TransferAttempt();
		transferAttempt.setTransfer(transfer);
		IRODSAccount irodsAccount = IRODSAccount.instance("host", 1247, "user",
				"password", "", "zone", "");

		TransferStatus transferStatus = TransferStatus.instance(
				TransferStatus.TransferType.PUT, "zoop", "blah", "", 0, 0, 0,
				0, 0, TransferState.OVERALL_INITIATION, "host", "zone");

		DefaultTransferControlBlock.instance();

		ConveyorService conveyorService = Mockito.mock(ConveyorService.class);
		QueueManagerService queueManagerService = Mockito
				.mock(QueueManagerService.class);
		Mockito.when(conveyorService.getQueueManagerService()).thenReturn(
				queueManagerService);

		Microservice enqueueTransferMicroservice = new EnqueueTransferMicroservice();
		InvocationContext invocationContext = new InvocationContext();
		ContainerEnvironment containerEnvironment = new ContainerEnvironment();
		containerEnvironment.setConveyorService(conveyorService);

		invocationContext.setTransferAttempt(transferAttempt);
		invocationContext.setIrodsAccount(irodsAccount);

		invocationContext.getSharedProperties().put(
				EnqueueTransferMicroservice.RESOURCE, "boo");

		enqueueTransferMicroservice.setInvocationContext(invocationContext);
		enqueueTransferMicroservice
				.setContainerEnvironment(containerEnvironment);
		ExecResult result = enqueueTransferMicroservice.execute(transferStatus);
		Assert.assertEquals("should get continue as exec result",
				ExecResult.CONTINUE, result);

		Transfer updatedTransfer = (Transfer) invocationContext
				.getSharedProperties().get(
						EnqueueTransferMicroservice.ENQUEUED_TRANSFER);

		Assert.assertNotNull("no transfer in shared context", updatedTransfer);

		Assert.assertEquals("resource path not updated", "boo",
				updatedTransfer.getResourceName());

	}

}
