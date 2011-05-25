package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.hibernate.Criteria;
import org.irods.jargon.transfer.dao.ConfigurationPropertyDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;
import org.irods.jargon.transfer.dao.domain.SynchConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * DAO for persistence of configuration properties.  This is where preferences and other data are stored.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ConfigurationPropertyDAOImpl  extends HibernateDaoSupport implements ConfigurationPropertyDAO {
	
	private static final Logger log = LoggerFactory
	.getLogger(ConfigurationPropertyDAOImpl.class);

	@Override
	public void saveOrUpdate(ConfigurationProperty configurationProperty)
			throws TransferDAOException {
		log.info("entering save(ConfigurationProperty)");

		this.getSessionFactory().getCurrentSession()
				.saveOrUpdate(configurationProperty);
		
	}

	@Override
	public ConfigurationProperty findById(Long id) throws TransferDAOException {
		log.info("entering findById with id:{}", id);
		return (ConfigurationProperty) this.getSessionFactory()
		.getCurrentSession().get(ConfigurationProperty.class, id);
	}

	@Override
	public List<ConfigurationProperty> findAll() throws TransferDAOException {
		List<ConfigurationProperty> retList = null;
		Criteria criteria = this.getSessionFactory().getCurrentSession()
				.createCriteria(ConfigurationProperty.class);

		retList = criteria.list();
		return retList;
	}

	@Override
	public void delete(ConfigurationProperty configurationProperty)
			throws TransferDAOException {
		// TODO Auto-generated method stub
		
	}


}
