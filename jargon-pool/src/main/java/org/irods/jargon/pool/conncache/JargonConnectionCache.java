/**
 *
 */
package org.irods.jargon.pool.conncache;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSMidLevelProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Connection cache keeps a pool of managed iRODS connections
 *
 * @author mconway
 *
 */
public class JargonConnectionCache extends GenericKeyedObjectPool<IRODSAccount, IRODSMidLevelProtocol> {

	public static final Logger log = LogManager.getLogger(JargonPooledObjectFactory.class);

	public JargonConnectionCache(final KeyedPooledObjectFactory<IRODSAccount, IRODSMidLevelProtocol> factory,
			final JargonKeyedPoolConfig config) {
		super(factory, config);
		setMaxIdlePerKey(config.getMaxIdlePerKey());
		setMinEvictableIdleTimeMillis(30000);
		setTimeBetweenEvictionRunsMillis(45000);
		setTestOnBorrow(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.commons.pool2.impl.GenericKeyedObjectPool#close()
	 */
	@Override
	public void close() {
		log.info("close()");
		this.clear();
	}

}
