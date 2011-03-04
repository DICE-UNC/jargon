/**
 * 
 */
package org.irods.jargon.core.pub.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.IRODSFileSystemAOImpl;
import org.irods.jargon.core.pub.IRODSGenericAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create IRODS File objects, will handle initialization of iRODS
 * connections and other non-file aspects
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class IRODSFileFactoryImpl extends IRODSGenericAO implements
		IRODSFileFactory {

	public static final String PATH_SEPARATOR = "/";
	static Logger log = LoggerFactory.getLogger(IRODSFileFactoryImpl.class);

	// TODO: switch to instance

	public IRODSFileFactoryImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFile(java.
	 * lang.String)
	 */
	@Override
	public IRODSFile instanceIRODSFile(final String path)
			throws JargonException {
		IRODSFileSystemAO irodsFileSystem = new IRODSFileSystemAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		return new IRODSFileImpl(path, irodsFileSystem);
	}
	
	/* (non-Javadoc)
	 * @see org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFileIndicatingType(java.lang.String, boolean)
	 */
	@Override
	public IRODSFile instanceIRODSFileIndicatingType(final String path, final boolean isFile)
			throws JargonException {
		IRODSFileSystemAO irodsFileSystem = new IRODSFileSystemAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		return new IRODSFileImpl(path, irodsFileSystem, isFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFile(java.
	 * net.URI)
	 */
	@Override
	public IRODSFile instanceIRODSFile(final URI uri) throws JargonException {
		// TODO: how about a URI that is outside of any
		// cache or pool, this
		// might have some impact on irodssession? Potential that arbitrary
		// connections would need to
		// happen given a URI.

		if (uri == null) {
			throw new JargonException("null uri");
		}

		if (!uri.getScheme().equals("irods")) {
			throw new JargonException("uri scheme is not irods, was:"
					+ uri.getScheme());
		}

		String userInfo = uri.getUserInfo();
		String userName = null;
		String password = "";
		String zone = "";
		String homeDirectory = null;
		int index = -1;

		index = userInfo.indexOf(":");
		if (index >= 0) {
			password = userInfo.substring(index + 1); // password
			userInfo = userInfo.substring(0, index);
		}

		index = userInfo.indexOf(".");
		if (index >= 0) {
			userName = userInfo.substring(0, index);
			zone = userInfo.substring(index + 1);
			homeDirectory = PATH_SEPARATOR + zone + PATH_SEPARATOR + userName;
		} else {
			userName = userInfo;
			homeDirectory = uri.getPath();
		}

		log.debug("userName: {}", userName);
		log.debug("home dir: {}", homeDirectory);
		log.debug("zone: {}", zone);

		IRODSAccount irodsAccountFromUri = IRODSAccount.instance(uri.getHost(),
				uri.getPort(), userName, password, homeDirectory, zone, "");

		String fileName = uri.getPath();

		log.debug("irods account: {}", irodsAccountFromUri.toString());
		log.debug("fileName: {}", fileName);

		IRODSFileSystemAO irodsFileSystem = new IRODSFileSystemAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		return new IRODSFileImpl(uri.getPath(), irodsFileSystem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFile(java.
	 * lang.String, java.lang.String)
	 */
	@Override
	public IRODSFile instanceIRODSFile(final String parent, final String child)
			throws JargonException {

		if (parent == null) {
			throw new JargonException("parent is null");
		}

		if (child == null) {
			throw new JargonException("child is null");
		}

		if (child.isEmpty() && parent.isEmpty()) {
			throw new JargonException("both child and parent names are blank");
		}

		IRODSFileSystemAO irodsFileSystem = new IRODSFileSystemAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());

		// check for blank parent, and "/" as child

		if (parent.isEmpty() && child.equals("/")) {
			return new IRODSFileImpl(child, irodsFileSystem);
		} else {
			return new IRODSFileImpl(parent, child, irodsFileSystem);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFile(java.
	 * io.File, java.lang.String)
	 */
	@Override
	public IRODSFile instanceIRODSFile(final File parent, final String child)
			throws JargonException {

		if (parent == null) {
			throw new JargonException("parent is null");
		}

		if (child == null) {
			throw new JargonException("child is null");
		}

		IRODSFileSystemAO irodsFileSystem = new IRODSFileSystemAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		return new IRODSFileImpl(parent, child, irodsFileSystem);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFileOutputStream
	 * (org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public IRODSFileOutputStream instanceIRODSFileOutputStream(
			final IRODSFile file) throws JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		try {
			if (!file.exists()) {
				log.info("file does not exist, creating a new file");
				file.createNewFile();
			} else if (!file.canWrite()) {
				log.info("this file is not writeable by the current user {}",
						file.getAbsolutePath());
				throw new JargonException("file is not writeable:"
						+ file.getAbsolutePath());
			}

			return new IRODSFileOutputStream(file, fileIOOperations);
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating output stream", e);
			throw new JargonException(e);
		} catch (IOException ioException) {
			log.error("IOException creating output stream", ioException);
			throw new JargonException(ioException);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFileOutputStream
	 * (java.lang.String)
	 */
	@Override
	public IRODSFileOutputStream instanceIRODSFileOutputStream(final String name)
			throws JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		try {
			if (log.isInfoEnabled()) {
				log.info("creating IRODSFileImpl for:" + name);
			}
			IRODSFile irodsFile = instanceIRODSFile(name);
			irodsFile.createNewFile();
			return new IRODSFileOutputStream(irodsFile, fileIOOperations);
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating output stream", e);
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("IOException creating output stream", e);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFileWriter
	 * (java.lang.String)
	 */
	@Override
	public IRODSFileWriter instanceIRODSFileWriter(final String name)
			throws JargonException {

		try {
			if (log.isInfoEnabled()) {
				log.info("creating IRODSFileWriter for:" + name);
			}
			IRODSFile irodsFile = instanceIRODSFile(name);
			irodsFile.createNewFile();
			return new IRODSFileWriter(irodsFile, this);
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating FileWriter", e);
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("IOException creating FileWriter", e);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFileReader
	 * (java.lang.String)
	 */
	@Override
	public IRODSFileReader instanceIRODSFileReader(
			final String irodsFileAbsolutePath) throws JargonException {

		try {
			if (log.isInfoEnabled()) {
				log.info("creating IRODSFileReader for:"
						+ irodsFileAbsolutePath);
			}
			IRODSFile irodsFile = instanceIRODSFile(irodsFileAbsolutePath);
			// must exist and be a file

			if (!irodsFile.exists()) {
				throw new JargonException("file does not exist in iRODS");
			}

			if (!irodsFile.isFile()) {
				throw new JargonException(
						"the given file is not a file in iRODS");
			}

			return new IRODSFileReader(irodsFile, this);
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating FileReader", e);
			throw new JargonException(e);
		} catch (IOException e) {
			log.error("IOException creating FileReader", e);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFileInputStream
	 * (org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public IRODSFileInputStream instanceIRODSFileInputStream(
			final IRODSFile file) throws JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		try {
			return new IRODSFileInputStream(file, fileIOOperations);
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating input stream", e);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFileInputStream
	 * (java.lang.String)
	 */
	@Override
	public SessionClosingIRODSFileInputStream instanceSessionClosingIRODSFileInputStream(
			final String name) throws JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		try {
			if (log.isInfoEnabled()) {
				log.info("opening IRODSFileImpl for:" + name);
			}
			IRODSFile irodsFile = instanceIRODSFile(name);

			return new SessionClosingIRODSFileInputStream(irodsFile,
					fileIOOperations);
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating output stream", e);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFileFactory#
	 * instanceSessionClosingIRODSFileInputStream
	 * (org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public SessionClosingIRODSFileInputStream instanceSessionClosingIRODSFileInputStream(
			final IRODSFile file) throws JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		try {
			return new SessionClosingIRODSFileInputStream(file,
					fileIOOperations);
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating input stream", e);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFileInputStream
	 * (java.lang.String)
	 */
	@Override
	public IRODSFileInputStream instanceIRODSFileInputStream(final String name)
			throws JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		try {
			if (log.isInfoEnabled()) {
				log.info("opening IRODSFileImpl for:" + name);
			}
			IRODSFile irodsFile = instanceIRODSFile(name);

			return new IRODSFileInputStream(irodsFile, fileIOOperations);
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating output stream", e);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSRandomAccessFile
	 * (java.lang.String)
	 */
	@Override
	public IRODSRandomAccessFile instanceIRODSRandomAccessFile(final String name)
			throws JargonException {
		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		log.info("opening IRODSFileImpl for: {}", name);
		IRODSFile irodsFile = instanceIRODSFile(name);

		if (!irodsFile.exists()) {
			log.info("requested file does not exist, will be created");
			try {
				irodsFile.createNewFile();
			} catch (IOException e) {
				log.error(
						"IOException creating file for IRODSRandomAccessFile will be rethrown as JargonException",
						e);
				throw new JargonException(e);
			}
		}

		// open the file if it is not opened
		irodsFile.open();
		return new IRODSRandomAccessFile(irodsFile, fileIOOperations);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSRandomAccessFile
	 * (org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public IRODSRandomAccessFile instanceIRODSRandomAccessFile(
			final IRODSFile irodsFile) throws JargonException {
		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		log.info("opening IRODSFileImpl for: {}", irodsFile.getAbsoluteFile());

		if (!irodsFile.exists()) {
			log.info("requested file does not exist, will be created");
			try {
				irodsFile.createNewFile();
			} catch (IOException e) {
				log.error(
						"IOException creating file for IRODSRandomAccessFile will be rethrown as JargonException",
						e);
				throw new JargonException(e);
			}
		}

		// open the file if it is not opened
		irodsFile.open();
		return new IRODSRandomAccessFile(irodsFile, fileIOOperations);
	}

}
