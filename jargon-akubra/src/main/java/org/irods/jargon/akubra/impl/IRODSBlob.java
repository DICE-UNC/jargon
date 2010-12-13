/**
 * 
 */
package org.irods.jargon.akubra.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import org.akubraproject.Blob;
import org.akubraproject.BlobStoreConnection;
import org.akubraproject.DuplicateBlobException;
import org.akubraproject.MissingBlobException;
import org.akubraproject.impl.AbstractBlob;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps an <code>IRODSFile</code> and provides acces to basic IO methods
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class IRODSBlob extends AbstractBlob {

	private final  IRODSFile irodsFile;
	public static final Logger log = LoggerFactory.getLogger(IRODSBlob.class);

	
	protected IRODSBlob(final BlobStoreConnection owner, final URI id, final IRODSFile irodsFile) {
		super(owner, id);
		
		if (irodsFile == null) {
			throw new IllegalArgumentException("IRODS file is null");
		}
		
		this.irodsFile = irodsFile;
		log.info("created IRODSBlog for underlying file:{}", irodsFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akubraproject.Blob#delete()
	 */
	public void delete() throws IOException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akubraproject.Blob#exists()
	 */

	public boolean exists() throws IOException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akubraproject.Blob#getSize()
	 */

	public long getSize() throws IOException, MissingBlobException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akubraproject.Blob#moveTo(java.net.URI, java.util.Map)
	 */
	public Blob moveTo(URI blobId, Map<String, String> hints)
			throws DuplicateBlobException, IOException, MissingBlobException,
			NullPointerException, IllegalArgumentException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akubraproject.Blob#openInputStream()
	 */

	public InputStream openInputStream() throws IOException,
			MissingBlobException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akubraproject.Blob#openOutputStream(long, boolean)
	 */

	public OutputStream openOutputStream(long estimatedSize, boolean overwrite)
			throws IOException, DuplicateBlobException {
		return null;
	}

}
