package org.irods.jargon.transferengine;

public class DatabasePreparationUtils {

	public static final void makeSureDatabaseIsInitialized() throws Exception {
		TransferQueueService transferQueueService = TransferQueueService
				.instance();
		transferQueueService.getCurrentQueue();
	}
}
