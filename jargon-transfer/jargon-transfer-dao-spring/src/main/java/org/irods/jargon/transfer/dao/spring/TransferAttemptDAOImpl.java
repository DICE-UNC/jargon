package org.irods.jargon.transfer.dao.spring;

import java.util.List;
import org.irods.jargon.transfer.dao.TransferAttemptDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 *
 * @author lisa
 */
public class TransferAttemptDAOImpl extends HibernateDaoSupport
		implements TransferAttemptDAO {

    @Override
    public void save(TransferAttempt ea) throws TransferDAOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TransferAttempt findById(Long id) throws TransferDAOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(TransferAttempt ea) throws TransferDAOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<TransferAttempt> findByTransferAttemptStatus(int maxResults, TransferStatus... transferStatus) throws TransferDAOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
