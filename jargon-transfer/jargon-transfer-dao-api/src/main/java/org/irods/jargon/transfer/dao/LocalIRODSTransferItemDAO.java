package org.irods.jargon.transfer.dao;

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
     * @param ea
     * @throws TransferDAOException
     */
    public void delete(LocalIRODSTransferItem ea) throws TransferDAOException;

}
