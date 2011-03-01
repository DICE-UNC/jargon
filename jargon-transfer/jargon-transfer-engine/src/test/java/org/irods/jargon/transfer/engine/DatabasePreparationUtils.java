package org.irods.jargon.transfer.engine;


public class DatabasePreparationUtils {

	public static final void makeSureDatabaseIsInitialized() throws Exception {
		TransferQueueService transferQueueService = new TransferQueueService();
		transferQueueService.getCurrentQueue();
	}
}
