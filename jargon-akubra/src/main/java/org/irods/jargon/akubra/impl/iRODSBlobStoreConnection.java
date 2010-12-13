/**
 * 
 */
package org.irods.jargon.akubra.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import org.akubraproject.AkubraException;
import org.akubraproject.Blob;
import org.akubraproject.BlobStore;
import org.akubraproject.UnsupportedIdException;
import org.akubraproject.impl.AbstractBlobStoreConnection;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jargon implementation of a blob store. that can produce an
 * {@link org.irods.jargon.akubra.impl.IRODSBlob}.  The <code>IRODSBlob</code> is a wrapper
 * around an {@link org.irods.jargon.core.pub.io.IRODSFile}.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class iRODSBlobStoreConnection extends AbstractBlobStoreConnection {
	
	private final IRODSFileFactory irodsFileFactory;
	public static Logger log = LoggerFactory.getLogger(IRODSBlobStore.class);

	public iRODSBlobStoreConnection(final BlobStore owner, final IRODSFileFactory irodsFileFactory) throws AkubraException {
		super(owner);
		if (irodsFileFactory == null) {
			throw new IllegalArgumentException("irodsFileFactory is null");
		}
		
		if (owner == null) {
			throw new IllegalArgumentException("null owner");
		}
		
		this.irodsFileFactory = irodsFileFactory;
	}

	/* (non-Javadoc)
	 * @see org.akubraproject.BlobStoreConnection#getBlob(java.net.URI, java.util.Map)
	 */
	public Blob getBlob(URI blobId, Map<String, String> hints)
			throws IOException, UnsupportedIdException,
			UnsupportedOperationException {
		
		if (blobId == null) {
			throw new IllegalArgumentException("blobId was null");
		}
		
		log.debug("getting blob for URI: {}", blobId);
		
		IRODSFile irodsFile;
		try {
			irodsFile = irodsFileFactory.instanceIRODSFile(blobId);
		} catch (JargonException e) {
			log.error("Jargon Exception when attempting to create Blob for blobId: {}", blobId, e);
			throw new AkubraException("error from jargon", e);
		}
		return new IRODSBlob(this, blobId, irodsFile);
	}

	/* (non-Javadoc)
	 * @see org.akubraproject.BlobStoreConnection#listBlobIds(java.lang.String)
	 */
	public Iterator<URI> listBlobIds(String filterPrefix) throws IOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.akubraproject.BlobStoreConnection#sync()
	 */
	public void sync() throws IOException, UnsupportedOperationException {

	}

}
