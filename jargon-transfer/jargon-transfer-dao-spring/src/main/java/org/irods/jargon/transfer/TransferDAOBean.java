package org.irods.jargon.transfer;

import org.irods.jargon.transfer.dao.ConfigurationPropertyDAO;
import org.irods.jargon.transfer.dao.LocalIRODSTransferDAO;
import org.irods.jargon.transfer.dao.LocalIRODSTransferItemDAO;
import org.irods.jargon.transfer.dao.SynchronizationDAO;

/**
 * 
 * @author jdr0887
 * 
 */
public class TransferDAOBean {

    private SynchronizationDAO synchronizationDAO;

    private ConfigurationPropertyDAO configurationPropertyDAO;

    private LocalIRODSTransferDAO localIRODSTransferDAO;

    private LocalIRODSTransferItemDAO localIRODSTransferItemDAO;

    public TransferDAOBean() {
        super();
    }

    /**
     * @return the synchronizationDAO
     */
    public SynchronizationDAO getSynchronizationDAO() {
        return synchronizationDAO;
    }

    /**
     * @param synchronizationDAO
     *            the synchronizationDAO to set
     */
    public void setSynchronizationDAO(SynchronizationDAO synchronizationDAO) {
        this.synchronizationDAO = synchronizationDAO;
    }

    /**
     * @return the configurationPropertyDAO
     */
    public ConfigurationPropertyDAO getConfigurationPropertyDAO() {
        return configurationPropertyDAO;
    }

    /**
     * @param configurationPropertyDAO
     *            the configurationPropertyDAO to set
     */
    public void setConfigurationPropertyDAO(ConfigurationPropertyDAO configurationPropertyDAO) {
        this.configurationPropertyDAO = configurationPropertyDAO;
    }

    /**
     * @return the localIRODSTransferDAO
     */
    public LocalIRODSTransferDAO getLocalIRODSTransferDAO() {
        return localIRODSTransferDAO;
    }

    /**
     * @param localIRODSTransferDAO
     *            the localIRODSTransferDAO to set
     */
    public void setLocalIRODSTransferDAO(LocalIRODSTransferDAO localIRODSTransferDAO) {
        this.localIRODSTransferDAO = localIRODSTransferDAO;
    }

    /**
     * @return the localIRODSTransferItemDAO
     */
    public LocalIRODSTransferItemDAO getLocalIRODSTransferItemDAO() {
        return localIRODSTransferItemDAO;
    }

    /**
     * @param localIRODSTransferItemDAO
     *            the localIRODSTransferItemDAO to set
     */
    public void setLocalIRODSTransferItemDAO(LocalIRODSTransferItemDAO localIRODSTransferItemDAO) {
        this.localIRODSTransferItemDAO = localIRODSTransferItemDAO;
    }

}
