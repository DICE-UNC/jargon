package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.irods.jargon.transfer.dao.TransferAttemptDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.Transfer;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.TransferAttemptDAO#save(org.irods.jargon
	 * .transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void save(TransferAttempt transferAttempt)
			throws TransferDAOException {
		logger.info("save()");
		this.getSessionFactory().getCurrentSession()
				.saveOrUpdate(transferAttempt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.TransferAttemptDAO#findById(java.lang.Long)
	 */
	@Override
	public TransferAttempt findById(Long id) throws TransferDAOException {
		logger.debug("entering findById(Long)");
		return (TransferAttempt) this.getSessionFactory().getCurrentSession()
				.get(TransferAttempt.class, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.TransferAttemptDAO#delete(org.irods.jargon
	 * .transfer.dao.domain.TransferAttempt)
	 */
	@Override
	public void delete(TransferAttempt ea) throws TransferDAOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.TransferAttemptDAO#findByTransferAttemptStatus
	 * (int, org.irods.jargon.transfer.dao.domain.TransferStatus[])
	 */
	@Override
	public List<TransferAttempt> findByTransferAttemptStatus(int maxResults,
			TransferStatus... transferStatus) throws TransferDAOException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.TransferAttemptDAO#
	 * findLastTransferAttemptForTransferByTransferId(long)
	 */
	@Override
	public TransferAttempt findLastTransferAttemptForTransferByTransferId(
			long transferId) throws TransferDAOException {

		Transfer transfer = (Transfer) this.getSessionFactory()
				.getCurrentSession().get(Transfer.class, transferId);

		if (transfer == null) {
			return null;
		}

		TransferAttempt attempts[] = new TransferAttempt[transfer
				.getTransferAttempts().size()];
		attempts = transfer.getTransferAttempts().toArray(attempts);

		if (attempts.length == 0) {
			return null;
		}

		return attempts[attempts.length - 1];

	}

}
