package org.irods.jargon.transfer.engine;

import org.irods.jargon.transfer.TransferServiceFactoryImpl;


public class DatabasePreparationUtils {

	public static final void makeSureDatabaseIsInitialized() throws Exception {
		TransferServiceFactoryImpl transferServiceFactory = new TransferServiceFactoryImpl();
		TransferQueueService transferQueueService = transferServiceFactory.instanceTransferQueueService();
		transferQueueService.getCurrentQueue();
	}
}
