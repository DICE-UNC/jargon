/**
 *
 */
package org.irods.jargon.pool.conncache;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.irods.jargon.core.connection.AbstractIRODSMidLevelProtocol;
import org.irods.jargon.core.connection.IRODSAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection cache keeps a pool of managed iRODS connections
 *
 * @author mconway
 *
 */
public class JargonConnectionCache extends GenericKeyedObjectPool<IRODSAccount, AbstractIRODSMidLevelProtocol> {

	public static final Logger log = LoggerFactory.getLogger(JargonPooledObjectFactory.class);

	public JargonConnectionCache(final KeyedPooledObjectFactory<IRODSAccount, AbstractIRODSMidLevelProtocol> factory,
			final JargonKeyedPoolConfig config) {
		super(factory);
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
