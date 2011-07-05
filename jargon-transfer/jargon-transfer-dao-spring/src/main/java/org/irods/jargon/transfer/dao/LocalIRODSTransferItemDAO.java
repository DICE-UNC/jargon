package org.irods.jargon.transfer.dao;

import java.util.List;

import org.irods.jargon.transfer.dao.domain.LocalIRODSTransferItem;

/**
 * 
 * @author jdr0887
 * 
 */
public interface LocalIRODSTransferItemDAO {

    /**
     * 
     * @param ea
     * @throws DAOException
     */
    public void save(LocalIRODSTransferItem ea) throws TransferDAOException;

    /**
     * 
     * @param id
     * @return
     * @throws DAOException
     */
    public LocalIRODSTransferItem findById(Long id) throws TransferDAOException;

    /**
     * 
     * @param id
     * @return
     * @throws TransferDAOException
     */
    public List<LocalIRODSTransferItem> findErrorItemsByTransferId(Long id) throws TransferDAOException;

    /**
     * 
     * @param ea
     * @throws TransferDAOException
     */
    public void delete(LocalIRODSTransferItem ea) throws TransferDAOException;

    /**
     * 
     * @param localIRODSTransferId
     * @return
     */
    public List<LocalIRODSTransferItem> findAllItemsForTransferByTransferId(Long localIRODSTransferId)
            throws TransferDAOException;

}
