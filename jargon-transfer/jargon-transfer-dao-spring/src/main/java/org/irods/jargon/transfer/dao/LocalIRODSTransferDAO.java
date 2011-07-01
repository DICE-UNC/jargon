package org.irods.jargon.transfer.dao;

import java.util.List;

import org.irods.jargon.transfer.dao.domain.LocalIRODSTransfer;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferStatus;

/**
 * 
 * @author jdr0887
 * 
 */
public interface LocalIRODSTransferDAO {

    /**
     * 
     * @param ea
     * @throws DAOException
     */
    public void save(LocalIRODSTransfer ea) throws TransferDAOException;

    /**
     * 
     * @param id
     * @return
     * @throws DAOException
     */
    public LocalIRODSTransfer findById(Long id) throws TransferDAOException;

    /**
     * 
     * @param id
     * @return
     * @throws TransferDAOException
     */
    public LocalIRODSTransfer findInitializedById(Long id) throws TransferDAOException;

    /**
     * 
     * @param transferState
     * @return
     * @throws TransferDAOException
     */
    public List<LocalIRODSTransfer> findByTransferState(TransferState... transferState) throws TransferDAOException;

    /**
     * 
     * @param maxResults
     * @return
     * @throws TransferDAOException
     */
    public List<LocalIRODSTransfer> findAllSortedDesc(int maxResults) throws TransferDAOException;

    /**
     * 
     * @param maxResults
     * @return
     * @throws TransferDAOException
     */
    public List<LocalIRODSTransfer> findAll() throws TransferDAOException;

    /**
     * 
     * @param maxResults
     * @param transferState
     * @return
     * @throws TransferDAOException
     */
    public List<LocalIRODSTransfer> findByTransferState(int maxResults, TransferState... transferState)
            throws TransferDAOException;

    /**
     * 
     * @param maxResults
     * @param transferStatus
     * @return
     * @throws TransferDAOException
     */
    public List<LocalIRODSTransfer> findByTransferStatus(int maxResults, TransferStatus... transferStatus)
            throws TransferDAOException;

    /**
     * 
     * @param notIn
     * @param transferState
     * @throws TransferDAOException
     */
    public void purgeQueue() throws TransferDAOException;

    /**
     * 
     * @throws TransferDAOException
     */
    public void purgeSuccessful() throws TransferDAOException;

    /**
     * 
     * @param ea
     * @throws TransferDAOException
     */
    public void delete(LocalIRODSTransfer ea) throws TransferDAOException;

}
