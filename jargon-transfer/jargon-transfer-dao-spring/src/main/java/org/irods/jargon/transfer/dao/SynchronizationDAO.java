package org.irods.jargon.transfer.dao;

import java.util.List;

import org.irods.jargon.transfer.dao.domain.Synchronization;

/**
 * 
 * @author jdr0887
 * 
 */
public interface SynchronizationDAO {

    /**
     * 
     * @param ea
     * @throws DAOException
     */
    public void save(Synchronization ea) throws TransferDAOException;

    /**
     * 
     * @param id
     * @return
     * @throws DAOException
     */
    public Synchronization findById(Long id) throws TransferDAOException;

    /**
     * 
     * @param id
     * @return
     * @throws TransferDAOException
     */
    public Synchronization findByName(String name) throws TransferDAOException;

    /**
     * 
     * @return
     * @throws TransferDAOException
     */
    public List<Synchronization> findAll() throws TransferDAOException;

    /**
     * 
     * @param ea
     * @throws TransferDAOException
     */
    public void delete(Synchronization ea) throws TransferDAOException;

}
