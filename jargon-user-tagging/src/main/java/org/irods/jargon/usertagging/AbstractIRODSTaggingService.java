package org.irods.jargon.usertagging;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.utils.MiscIRODSUtils;

public abstract class AbstractIRODSTaggingService {

	protected final IRODSAccessObjectFactory irodsAccessObjectFactory;
	protected final IRODSAccount irodsAccount;

	/**
	 * Private constructor that initializes the service with access to objects
	 * that interact with iRODS.
	 * 
	 * @param irodsAccessObjectFactory
	 *            <code>IRODSAccessObjectFactory</code> that can create various
	 *            iRODS Access Objects.
	 * @param irodsAccount
	 *            <code>IRODSAccount</code> that describes the target server and
	 *            credentials.
	 */
	protected AbstractIRODSTaggingService(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;

	}

	protected IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	protected IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}
	
	/**
	 * @param irodsAbsolutePath
	 * @return
	 * @throws JargonException
	 * @throws FileNotFoundException
	 */
	protected ObjStat getObjStatForAbsolutePath(final String irodsAbsolutePath)
			throws JargonException, FileNotFoundException {
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = getIrodsAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(getIrodsAccount());

		ObjStat objStat = collectionAndDataObjectListAndSearchAO
				.retrieveObjectStatForPath(irodsAbsolutePath);

		MiscIRODSUtils.evaluateSpecCollSupport(objStat);
		return objStat;
	}

}