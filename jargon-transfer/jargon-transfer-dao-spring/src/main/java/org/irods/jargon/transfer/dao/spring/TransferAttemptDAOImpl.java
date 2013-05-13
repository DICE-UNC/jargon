package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.irods.jargon.transfer.dao.TransferAttemptDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferStatus;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Database operations for the <code>TransferAttempt</code>
 * 
 * @author lisa
 */
public class TransferAttemptDAOImpl extends HibernateDaoSupport implements
		TransferAttemptDAO {

	@Override
	public void save(TransferAttempt transferAttempt)
			throws TransferDAOException {
		logger.info("save()");
		this.getSessionFactory().getCurrentSession()
				.saveOrUpdate(transferAttempt);
	}

	@Override
	public TransferAttempt findById(Long id) throws TransferDAOException {
		logger.debug("entering findById(Long)");
		return (TransferAttempt) this.getSessionFactory().getCurrentSession()
				.get(TransferAttempt.class, id);
	}

	@Override
	public void delete(TransferAttempt ea) throws TransferDAOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<TransferAttempt> findByTransferAttemptStatus(int maxResults,
			TransferStatus... transferStatus) throws TransferDAOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
