package org.irods.jargon.transfer.dao;

import org.irods.jargon.transfer.dao.domain.TransferAttempt;

/**
 *
 * @author lisa
 */
public interface TransferAttemptDAO {
    
    /**
	 * 
	 * @param ea
	 * @throws DAOException
	 */
	public void save(TransferAttempt ea) throws TransferDAOException;

	/**
	 * 
	 * @param id
	 * @return
	 * @throws DAOException
	 */
	public TransferAttempt findById(Long id) throws TransferDAOException;
        
        /**
	 * 
	 * @param ea
	 * @throws TransferDAOException
	 */
        public void delete(TransferAttempt ea) throws TransferDAOException;
    
}
