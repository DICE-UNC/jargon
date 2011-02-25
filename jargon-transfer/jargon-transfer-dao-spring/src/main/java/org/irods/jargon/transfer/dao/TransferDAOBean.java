package org.irods.jargon.transfer.dao;

import org.irods.jargon.transfer.dao.LocalIRODSTransferDAO;
import org.irods.jargon.transfer.dao.LocalIRODSTransferItemDAO;

/**
 * 
 * @author jdr0887
 *
 */
public class TransferDAOBean {

    private LocalIRODSTransferDAO localIRODSTransferDAO;

    private LocalIRODSTransferItemDAO localIRODSTransferItemDAO;

    public TransferDAOBean() {
        super();
    }

    public LocalIRODSTransferDAO getLocalIRODSTransferDAO() {
        return localIRODSTransferDAO;
    }

    public void setLocalIRODSTransferDAO(LocalIRODSTransferDAO localIRODSTransferDAO) {
        this.localIRODSTransferDAO = localIRODSTransferDAO;
    }

    public LocalIRODSTransferItemDAO getLocalIRODSTransferItemDAO() {
        return localIRODSTransferItemDAO;
    }

    public void setLocalIRODSTransferItemDAO(LocalIRODSTransferItemDAO localIRODSTransferItemDAO) {
        this.localIRODSTransferItemDAO = localIRODSTransferItemDAO;
    }

}
