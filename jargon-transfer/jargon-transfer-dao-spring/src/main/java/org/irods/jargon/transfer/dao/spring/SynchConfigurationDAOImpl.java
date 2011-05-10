package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.irods.jargon.transfer.dao.SynchConfigurationDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.SynchConfiguration;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class SynchConfigurationDAOImpl extends HibernateDaoSupport implements SynchConfigurationDAO {

	/* (non-Javadoc)
	 * @see org.irods.jargon.transfer.dao.SynchConfigurationDAO#save(org.irods.jargon.transfer.dao.domain.SynchConfiguration)
	 */
	@Override
	public void save(SynchConfiguration synchConfiguration)
			throws TransferDAOException {

	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.transfer.dao.SynchConfigurationDAO#findById(java.lang.Long)
	 */
	@Override
	public SynchConfiguration findById(Long id) throws TransferDAOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.transfer.dao.SynchConfigurationDAO#findInitializedById(java.lang.Long)
	 */
	@Override
	public SynchConfiguration findInitializedById(Long id)
			throws TransferDAOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.transfer.dao.SynchConfigurationDAO#findById(java.lang.Long, boolean)
	 */
	@Override
	public SynchConfiguration findById(Long id, boolean error)
			throws TransferDAOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.transfer.dao.SynchConfigurationDAO#findAll()
	 */
	@Override
	public List<SynchConfiguration> findAll() throws TransferDAOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.transfer.dao.SynchConfigurationDAO#delete(org.irods.jargon.transfer.dao.domain.SynchConfiguration)
	 */
	@Override
	public void delete(SynchConfiguration synchConfiguration)
			throws TransferDAOException {
	}

}
