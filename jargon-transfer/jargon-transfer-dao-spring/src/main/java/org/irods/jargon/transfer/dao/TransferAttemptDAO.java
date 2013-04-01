package org.irods.jargon.transfer.dao;

import java.util.List;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferStatus;

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
        
        /**
	 * 
	 * @param maxResults
	 * @param transferStatus
	 * @return
	 * @throws TransferDAOException
	 */
	public List<TransferAttempt> findByTransferAttemptStatus(int maxResults,
			TransferStatus... transferStatus) throws TransferDAOException;
    
}
