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
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.FileCatalogObjectAOImpl;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.IRODSFileSystemAOImpl;
import org.irods.jargon.core.pub.IRODSGenericAO;
import org.irods.jargon.core.utils.IRODSUriUtils;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFile(java.
	 * net.URI)
	 */
	@Override
	public IRODSFile instanceIRODSFile(final URI uri) throws JargonException {

		if (uri == null) {
			throw new JargonException("null uri");
		}

		IRODSAccount irodsAccount = null;
		try {
			irodsAccount = IRODSUriUtils.getIRODSAccountFromURI(uri);
		} catch (JargonException je) {
			log.info("no account info in URI, use default account");
			irodsAccount = this.getIRODSAccount();
		}

		String fileName = uri.getPath();

		log.debug("irods account: {}", irodsAccount.toString());
		log.debug("fileName: {}", fileName);

		IRODSFileSystemAO irodsFileSystemAO = new IRODSFileSystemAOImpl(
				this.getIRODSSession(), irodsAccount);
		return new IRODSFileImpl(uri.getPath(), irodsFileSystemAO);
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
			final IRODSFile file) throws NoResourceDefinedException,
			JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		try {
			return new IRODSFileOutputStream(file, fileIOOperations);
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating output stream", e);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFileFactory#
	 * instanceIRODSFileOutputStreamWithRerouting
	 * (org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public IRODSFileOutputStream instanceIRODSFileOutputStreamWithRerouting(
			final IRODSFile file) throws NoResourceDefinedException,
			JargonException {

		try {
			if (!file.exists()) {
				log.info("file does not exist, a new one will be created");
			} else if (!file.canWrite()) {
				log.info("this file is not writeable by the current user {}",
						file.getAbsolutePath());
				throw new JargonException("file is not writeable:"
						+ file.getAbsolutePath());
			}

			IRODSAccount useThisAccount = this.getIRODSAccount();
			boolean reroute = false;

			if (this.getIRODSServerProperties().isSupportsConnectionRerouting()) {
				log.info("redirects are available, check to see if I need to redirect to a resource server");
				DataObjectAO dataObjectAO = this.getIRODSAccessObjectFactory()
						.getDataObjectAO(getIRODSAccount());
				String detectedHost = dataObjectAO.getHostForPutOperation(
						file.getAbsolutePath(), file.getResource());

				if (detectedHost == null
						|| detectedHost
								.equals(FileCatalogObjectAOImpl.USE_THIS_ADDRESS)) {
					log.info("using given resource connection");
				} else if (detectedHost.equals("localhost")) {
					log.warn("localhost received as detected host, ignore and do not reroute");
				} else {
					useThisAccount = IRODSAccount.instanceForReroutedHost(
							getIRODSAccount(), detectedHost);
					reroute = true;
				}
			}

			FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
					this.getIRODSSession(), useThisAccount);

			if (reroute) {
				IRODSFileFactory rerouteFileFactory = getIRODSAccessObjectFactory()
						.getIRODSFileFactory(useThisAccount);
				IRODSFile irodsFile = rerouteFileFactory.instanceIRODSFile(file
						.getAbsolutePath());
				return new SessionClosingIRODSFileOutputStream(irodsFile,
						fileIOOperations);
			} else {
				IRODSFile irodsFile = instanceIRODSFile(file.getAbsolutePath());
				return new IRODSFileOutputStream(irodsFile, fileIOOperations);
			}
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating output stream", e);
			throw new JargonException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.io.IRODSFileFactory#
	 * instanceSessionClosingIRODSFileOutputStream
	 * (org.irods.jargon.core.pub.io.IRODSFile)
	 */
	@Override
	public SessionClosingIRODSFileOutputStream instanceSessionClosingIRODSFileOutputStream(
			final IRODSFile file) throws NoResourceDefinedException,
			JargonException {

		log.info("instanceSessionClosingIRODSFileOutputStream");
		if (file == null) {
			throw new IllegalArgumentException("null irodsFile");
		}

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		try {
			if (!file.exists()) {
				log.info("file does not exist, creating a new file");
				file.createNewFileCheckNoResourceFound();
			} else if (!file.canWrite()) {
				log.info("this file is not writeable by the current user {}",
						file.getAbsolutePath());
				throw new JargonException("file is not writeable:"
						+ file.getAbsolutePath());
			}

			return new SessionClosingIRODSFileOutputStream(file,
					fileIOOperations);
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
			throws NoResourceDefinedException, JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		try {
			if (log.isInfoEnabled()) {
				log.info("creating IRODSFileImpl for:" + name);
			}
			IRODSFile irodsFile = instanceIRODSFile(name);
			return new IRODSFileOutputStream(irodsFile, fileIOOperations);
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating output stream", e);
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
			throws NoResourceDefinedException, JargonException {

		try {
			if (log.isInfoEnabled()) {
				log.info("creating IRODSFileWriter for:" + name);
			}
			IRODSFile irodsFile = instanceIRODSFile(name);
			irodsFile.createNewFileCheckNoResourceFound();
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
	 * @see org.irods.jargon.core.pub.io.IRODSFileFactory#
	 * instanceIRODSFileInputStreamGivingFD
	 * (org.irods.jargon.core.pub.io.IRODSFile, int)
	 */
	@Override
	public IRODSFileInputStream instanceIRODSFileInputStreamGivingFD(
			final IRODSFile file, final int fd) throws JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		try {
			return new IRODSFileInputStream(file, fileIOOperations, fd);
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
			final String name) throws NoResourceDefinedException,
			JargonException {

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
	 * @see org.irods.jargon.core.pub.io.IRODSFileFactory#
	 * instanceIRODSFileInputStreamWithRerouting(java.lang.String)
	 */
	@Override
	public IRODSFileInputStream instanceIRODSFileInputStreamWithRerouting(
			final String irodsAbsolutePath) throws JargonException {

		IRODSAccount useThisAccount = this.getIRODSAccount();
		boolean reroute = false;

		if (this.getIRODSServerProperties().isSupportsConnectionRerouting()) {
			log.info("redirects are available, check to see if I need to redirect to a resource server");
			DataObjectAO dataObjectAO = this.getIRODSAccessObjectFactory()
					.getDataObjectAO(getIRODSAccount());
			String detectedHost = dataObjectAO.getHostForGetOperation(
					irodsAbsolutePath, "");

			if (detectedHost == null
					|| detectedHost
							.equals(FileCatalogObjectAOImpl.USE_THIS_ADDRESS)) {
				log.info("using given resource connection");
			} else {
				useThisAccount = IRODSAccount.instanceForReroutedHost(
						getIRODSAccount(), detectedHost);
				reroute = true;
			}
		}

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), useThisAccount);
		try {
			if (log.isInfoEnabled()) {
				log.info("opening IRODSFileImpl for:" + irodsAbsolutePath);
			}

			if (reroute) {
				IRODSFileFactory rerouteFileFactory = getIRODSAccessObjectFactory()
						.getIRODSFileFactory(useThisAccount);
				IRODSFile irodsFile = rerouteFileFactory
						.instanceIRODSFile(irodsAbsolutePath);
				return new SessionClosingIRODSFileInputStream(irodsFile,
						fileIOOperations);
			} else {
				IRODSFile irodsFile = instanceIRODSFile(irodsAbsolutePath);
				return new IRODSFileInputStream(irodsFile, fileIOOperations);
			}

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
			throws NoResourceDefinedException, JargonException {
		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		log.info("opening IRODSFileImpl for: {}", name);
		IRODSFile irodsFile = instanceIRODSFile(name);

		if (!irodsFile.exists()) {
			log.info("requested file does not exist, will be created");

			irodsFile.createNewFileCheckNoResourceFound();
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
			final IRODSFile irodsFile) throws NoResourceDefinedException,
			JargonException {
		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(
				this.getIRODSSession(), this.getIRODSAccount());
		log.info("opening IRODSFileImpl for: {}", irodsFile.getAbsoluteFile());

		if (!irodsFile.exists()) {
			log.info("requested file does not exist, will be created");

			irodsFile.createNewFileCheckNoResourceFound();
		}

		// open the file if it is not opened
		irodsFile.open();
		return new IRODSRandomAccessFile(irodsFile, fileIOOperations);
	}

}
