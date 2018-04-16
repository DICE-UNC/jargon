/**
 * 
 */
package org.irods.jargon.core.nio.provider;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserDefinedFileAttributeView;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;

/**
 * iRODS NIO FileStore implementation
 * 
 * @author conwaymc
 *
 */
public class IrodsNioFileStore extends FileStore {

	private final IRODSAccount irodsAccount;
	private final IRODSFileSystem irodsFileSystem;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileStore#name()
	 */
	@Override
	public String name() {
		return "irods";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileStore#type()
	 */
	@Override
	public String type() {
		// TODO Auto-generated method stub
		return "irods";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileStore#isReadOnly()
	 */
	@Override
	public boolean isReadOnly() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileStore#getTotalSpace()
	 */
	@Override
	public long getTotalSpace() throws IOException {
		return Long.MAX_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileStore#getUsableSpace()
	 */
	@Override
	public long getUsableSpace() throws IOException {
		return Long.MAX_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileStore#getUnallocatedSpace()
	 */
	@Override
	public long getUnallocatedSpace() throws IOException {
		return Long.MAX_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileStore#supportsFileAttributeView(java.lang.Class)
	 */
	@Override
	public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
		if (type == FileOwnerAttributeView.class) {
			return true;
		} else if (type == PosixFileAttributeView.class) {
			return true;
		} else if (type == UserDefinedFileAttributeView.class) {
			return true;
		} else {
			return false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileStore#supportsFileAttributeView(java.lang.String)
	 */
	@Override
	public boolean supportsFileAttributeView(String name) {
		if (name == null || name.isEmpty()) {
			return false;
		}
		if (name.equals(anObject))
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileStore#getFileStoreAttributeView(java.lang.Class)
	 */
	@Override
	public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.file.FileStore#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String attribute) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param irodsAccount
	 * @param irodsFileSystem
	 */
	private IrodsNioFileStore(IRODSAccount irodsAccount, IRODSFileSystem irodsFileSystem) {
		super();
		this.irodsAccount = irodsAccount;
		this.irodsFileSystem = irodsFileSystem;
	}

}
