package org.irods.jargon.transfer.dao;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.junit.Test;

public class LocalIRODSTransferDAOTest {

    private final TransferDAOManager transferDAOMgr = TransferDAOManager.getInstance();

    @Test
    public void testSave() {
        
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();
        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("/tmp");
        enqueuedTransfer.setLocalAbsolutePath("/tmp");
        enqueuedTransfer.setTransferHost("localhost");
        enqueuedTransfer.setTransferPort(1247);
        enqueuedTransfer.setTransferResource("test-resc1");
        enqueuedTransfer.setTransferZone("tempZone");
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName("test");
        enqueuedTransfer.setTransferPassword("test");
        enqueuedTransfer.setTransferState(TransferState.PROCESSING);
        enqueuedTransfer.setTransferStatus(TransferStatus.ERROR);
        
        assertTrue(enqueuedTransfer.getId() == null);
        try {
            localIRODSTransferDAO.save(enqueuedTransfer);
        } catch (TransferDAOException e) {
            e.printStackTrace();
        }
        assertTrue(enqueuedTransfer.getId() != null);
        
    }
    
    @Test
    public void testPurgeQueue() {
        LocalIRODSTransferDAO localIRODSTransferDAO = transferDAOMgr.getTransferDAOBean().getLocalIRODSTransferDAO();
        LocalIRODSTransfer enqueuedTransfer = new LocalIRODSTransfer();
        enqueuedTransfer.setCreatedAt(new Date());
        enqueuedTransfer.setIrodsAbsolutePath("/tmp");
        enqueuedTransfer.setLocalAbsolutePath("/tmp");
        enqueuedTransfer.setTransferHost("localhost");
        enqueuedTransfer.setTransferPort(1247);
        enqueuedTransfer.setTransferResource("test-resc1");
        enqueuedTransfer.setTransferZone("tempZone");
        enqueuedTransfer.setTransferStart(new Date());
        enqueuedTransfer.setTransferType(TransferType.PUT);
        enqueuedTransfer.setTransferUserName("test");
        enqueuedTransfer.setTransferPassword("test");
        enqueuedTransfer.setTransferState(TransferState.COMPLETE);
        enqueuedTransfer.setTransferStatus(TransferStatus.OK);
        
        assertTrue(enqueuedTransfer.getId() == null);
        try {
            localIRODSTransferDAO.save(enqueuedTransfer);
        } catch (TransferDAOException e) {
            e.printStackTrace();
        }
        assertTrue(enqueuedTransfer.getId() != null);
        
        try {
            localIRODSTransferDAO.purgeQueue();
            assertTrue(localIRODSTransferDAO.findByTransferState(TransferState.COMPLETE).size() == 0);
        } catch (TransferDAOException e) {
            e.printStackTrace();
        }
    }
    
    
}
