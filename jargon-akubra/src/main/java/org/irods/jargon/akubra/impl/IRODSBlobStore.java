/**
 * 
 */
package org.irods.jargon.akubra.impl;

import java.io.IOException;
import java.util.Map;

import javax.transaction.Transaction;

import org.akubraproject.AkubraException;
import org.akubraproject.BlobStoreConnection;
import org.akubraproject.impl.AbstractBlobStore;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the infrastructure that manages underlying IRODS connections and hands
 * those connections out to callers.
 * 
 * The <code>IRODSession</code> object is the key to obtaining connections to IRODS.  This <code>IRODSSession</code> object
 * holds a reference to a connection source of the interface <code>IRODSProtocolManager</code>.  The protocol manager is pluggable such that
 * it can simply return a connection per request, or can manage a cache, pool, or other connection management scheme.
 * 
 * The instance requires an <code>IRODSAccount</code> object which describe both the iRODS resource to connect to, as well as authentication 
 * information.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class IRODSBlobStore extends AbstractBlobStore {

	public final Logger log = LoggerFactory.getLogger(this.getClass());

	private transient final IRODSAccount irodsAccount;
	private transient final IRODSSession irodsSession;

	/**
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> with the necessary credentials to
	 *            connect to irods
	 * @param irodsSession
	 *            <code>IRODSSession</code> that provides a ThreadLocal cache of
	 *            connection
	 * @return instance of <code>IRODSBlobStore</code>
	 * @throws JargonException
	 * @throws AkubraException
	 */
	public static IRODSBlobStore instance(final IRODSAccount irodsAccount,
			final IRODSSession irodsSession) throws JargonException, AkubraException {
		return new IRODSBlobStore(irodsAccount, irodsSession);
	}

	private IRODSBlobStore(final IRODSAccount irodsAccount, final IRODSSession irodsSession)
			throws JargonException, AkubraException {

		super(irodsAccount.toURI(false));

		if (irodsSession == null) {
			throw new AkubraException("irodsSession is null");
		}

		log.info("opening an irodsBlobStore for account: {}", irodsAccount);

		this.irodsAccount = irodsAccount;
		this.irodsSession = irodsSession;
	}

	/* (non-Javadoc)
	 * @see org.akubraproject.BlobStore#openConnection(javax.transaction.Transaction, java.util.Map)
	 */
	public BlobStoreConnection openConnection(Transaction tx,
			final Map<String, String> hints) throws UnsupportedOperationException,
			IOException {
		
		if (tx != null) {
			throw new UnsupportedOperationException("a non-null tx parameter was passed, this driver does not support transactions");
		}
		
		if (hints != null) {
			if (!hints.isEmpty()) {
				log.warn("hints passed, but will be ignored");
			}
		}
		
		log.info("creating irods file factory");
		IRODSFileFactory irodsFileFactory;
		try {
			irodsFileFactory = new IRODSFileFactoryImpl(irodsSession, irodsAccount);
		} catch (JargonException e) {
			log.error("Jargon Exception when attempting to create irodsFileFactory for account: {}", irodsAccount.toString(), e);
			throw new AkubraException("error from jargon", e);
		}
	
		log.debug("returning new blob store connection");
		return new iRODSBlobStoreConnection(this, irodsFileFactory);
	}

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	public IRODSSession getIrodsSession() {
		return irodsSession;
	}

}
