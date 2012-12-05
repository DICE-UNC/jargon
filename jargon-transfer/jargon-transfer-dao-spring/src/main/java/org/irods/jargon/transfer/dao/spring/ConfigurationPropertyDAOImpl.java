package org.irods.jargon.transfer.dao.spring;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.irods.jargon.transfer.dao.ConfigurationPropertyDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * DAO for persistence of configuration properties. This is where preferences
 * and other data are stored.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ConfigurationPropertyDAOImpl extends HibernateDaoSupport implements
		ConfigurationPropertyDAO {

	private static final Logger log = LoggerFactory
			.getLogger(ConfigurationPropertyDAOImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.ConfigurationPropertyDAO#saveOrUpdate(org
	 * .irods.jargon.transfer.dao.domain.ConfigurationProperty)
	 */
	@Override
	public void saveOrUpdate(final ConfigurationProperty configurationProperty)
			throws TransferDAOException {
		log.info("entering save(ConfigurationProperty)");
		if (configurationProperty == null) {
			throw new IllegalArgumentException("null configurationProperty");
		}

		log.info("configurationProperty:{}", configurationProperty);

		this.getSessionFactory().getCurrentSession()
				.saveOrUpdate(configurationProperty);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.ConfigurationPropertyDAO#findById(java.
	 * lang.Long)
	 */
	@Override
	public ConfigurationProperty findById(final Long id)
			throws TransferDAOException {
		log.info("entering findById with id:{}", id);
		return (ConfigurationProperty) this.getSessionFactory()
				.getCurrentSession().get(ConfigurationProperty.class, id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.ConfigurationPropertyDAO#findByPropertyKey
	 * (java.lang.String)
	 */
	@Override
	public ConfigurationProperty findByPropertyKey(final String propertyKey)
			throws TransferDAOException {
		if (propertyKey == null || propertyKey.isEmpty()) {
			throw new IllegalArgumentException("null or empty property key");
		}
		log.info("findByPropertyKey key=", propertyKey);
		try {
			Criteria criteria = this.getSessionFactory().getCurrentSession()
					.createCriteria(ConfigurationProperty.class);
			criteria.add(Restrictions.eq("propertyKey", propertyKey));
			return (ConfigurationProperty) criteria.uniqueResult();

		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {
			log.error("error", e);
			throw new TransferDAOException("exception in findById", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.transfer.dao.ConfigurationPropertyDAO#findAll()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<ConfigurationProperty> findAll() throws TransferDAOException {
		Criteria criteria = this.getSessionFactory().getCurrentSession()
				.createCriteria(ConfigurationProperty.class);

		return criteria.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.ConfigurationPropertyDAO#delete(org.irods
	 * .jargon.transfer.dao.domain.ConfigurationProperty)
	 */
	@Override
	public void delete(final ConfigurationProperty configurationProperty)
			throws TransferDAOException {
		try {

			this.getSessionFactory().getCurrentSession()
					.delete(configurationProperty);
		} catch (HibernateException e) {
			log.error("HibernateException", e);
			throw new TransferDAOException(e);
		} catch (Exception e) {

			log.error("error in delete(ConfigurationProperty)", e);
			throw new TransferDAOException(
					"Failed delete(ConfigurationProperty)", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.transfer.dao.ConfigurationPropertyDAO#deleteAllProperties
	 * ()
	 */
	@Override
	public void deleteAllProperties() throws TransferDAOException {
		log.info("deleteAllProperties()");
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ConfigurationProperty as prop");

		log.debug("delete properties sql:{}", sb.toString());

		HibernateTemplate hibernateTemplate = super.getHibernateTemplate();

		int rows = hibernateTemplate.bulkUpdate(sb.toString());
		log.debug("deleted properties count of: {}", rows);

	}

}
