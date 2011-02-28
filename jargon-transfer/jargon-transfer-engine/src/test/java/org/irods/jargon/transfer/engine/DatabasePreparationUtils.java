package org.irods.jargon.transfer.engine;

import org.irods.jargon.transfer.engine.TransferQueueService;

public class DatabasePreparationUtils {

    public static final void makeSureDatabaseIsInitialized() throws Exception {
        TransferQueueService transferQueueService = new TransferQueueService();
        transferQueueService.getCurrentQueue();
    }
}
