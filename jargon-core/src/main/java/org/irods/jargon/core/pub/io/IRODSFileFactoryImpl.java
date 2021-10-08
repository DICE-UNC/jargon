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
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.FileCatalogObjectAOImpl;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.IRODSFileSystemAOImpl;
import org.irods.jargon.core.pub.IRODSGenericAO;
import org.irods.jargon.core.utils.IRODSUriUtils;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.perf4j.StopWatch;
//import org.perf4j.slf4j.Slf4JStopWatch;

/**
 * Factory to create IRODS File objects, will handle initialization of iRODS
 * connections and other non-file aspects
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class IRODSFileFactoryImpl extends IRODSGenericAO implements IRODSFileFactory {

	public static final String PATH_SEPARATOR = "/";
	static Logger log = LoggerFactory.getLogger(IRODSFileFactoryImpl.class);

	public IRODSFileFactoryImpl(final IRODSSession irodsSession, final IRODSAccount irodsAccount)
			throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFile(java.
	 * lang.String)
	 */
	@Override
	public IRODSFile instanceIRODSFile(final String path) throws JargonException {
		IRODSFileSystemAO irodsFileSystem = new IRODSFileSystemAOImpl(getIRODSSession(), getIRODSAccount());
		return new IRODSFileImpl(path, irodsFileSystem);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFileUserHomeDir
	 * (java.lang.String)
	 */
	@Override
	public IRODSFile instanceIRODSFileUserHomeDir(final String userName) throws JargonException {

		log.info("instanceIRODSFileUserHomeDir()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		IRODSFileSystemAO irodsFileSystem = new IRODSFileSystemAOImpl(getIRODSSession(), getIRODSAccount());

		String homePath = MiscIRODSUtils.computeHomeDirectoryForGivenUserInSameZoneAsIRODSAccount(getIRODSAccount(),
				userName);
		return new IRODSFileImpl(homePath, irodsFileSystem);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFile(java.
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
			irodsAccount = getIRODSAccount();
		}

		String fileName = uri.getPath();

		log.debug("irods account: {}", irodsAccount.toString());
		log.debug("fileName: {}", fileName);

		IRODSFileSystemAO irodsFileSystemAO = new IRODSFileSystemAOImpl(getIRODSSession(), irodsAccount);
		return new IRODSFileImpl(uri.getPath(), irodsFileSystemAO);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFile(java.
	 * lang.String, java.lang.String)
	 */
	@Override
	public IRODSFile instanceIRODSFile(final String parent, final String child) throws JargonException {

		log.info("instanceIRODSFile()");

		if (parent == null) {
			throw new JargonException("parent is null");
		}

		if (child == null) {
			throw new JargonException("child is null");
		}

		log.info("parent:{}", parent);
		log.info("child:{}", child);

		if (child.isEmpty() && parent.isEmpty()) {
			throw new JargonException("both child and parent names are blank");
		}

		if (child.isEmpty()) {
			return instanceIRODSFile(parent);
		}

		IRODSFileSystemAO irodsFileSystem = new IRODSFileSystemAOImpl(getIRODSSession(), getIRODSAccount());

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
	 * @see org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFile(java.
	 * io.File, java.lang.String)
	 */
	@Override
	public IRODSFile instanceIRODSFile(final File parent, final String child) throws JargonException {

		if (parent == null) {
			throw new JargonException("parent is null");
		}

		if (child == null) {
			throw new JargonException("child is null");
		}

		if (child.isEmpty()) {
			return instanceIRODSFile(parent.getAbsolutePath());
		}

		IRODSFileSystemAO irodsFileSystem = new IRODSFileSystemAOImpl(getIRODSSession(), getIRODSAccount());
		return new IRODSFileImpl(parent, child, irodsFileSystem);

	}

	/*
	 * FIXME: this will be added
	 */
	//
	// public IRODSFileOutputStream instanceCoordinatedIRODSFileOutputStream(final
	// String path,
//			final OpenFlags openFlags) {
//
//	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFileOutputStream
	 * (org.irods.jargon.core.pub.io.IRODSFile) FIXME: what if the replica token is
	 * already in the irods file?
	 */
	@Override
	public IRODSFileOutputStream instanceIRODSFileOutputStream(final IRODSFile file)
			throws NoResourceDefinedException, JargonException {

		log.info("instanceIRODSFileOutputStream()");
		return this.instanceIRODSFileOutputStream(file, OpenFlags.WRITE);
	}

	// add instanceIRODSFileOutputStream(IRODSFile, replicaToken);

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFileOutputStream
	 * (org.irods.jargon.core.pub.io.IRODSFile,
	 * org.irods.jargon.core.packinstr.DataObjInp.OpenFlags)
	 */
	@Override
	public IRODSFileOutputStream instanceIRODSFileOutputStream(final IRODSFile file, final OpenFlags openFlags)
			throws NoResourceDefinedException, JargonException {

		log.info("instanceIRODSFileOutputStream()");

		if (file == null) {
			throw new IllegalArgumentException("null file");
		}

		if (openFlags == null) {
			throw new IllegalArgumentException("null openFlags");
		}

		log.info("file:{}", file);
		log.info("openFlags:{}", openFlags);

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), getIRODSAccount());
		try {
			return new IRODSFileOutputStream(file, fileIOOperations, openFlags);
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating output stream", e);
			throw new JargonException(e);
		}
	}

	@Override
	public IRODSFileOutputStream instanceIRODSFileOutputStream(final IRODSFile file, final OpenFlags openFlags,
			final boolean coordinated) throws NoResourceDefinedException, JargonException {

		log.info("instanceIRODSFileOutputStream()");

		if (file == null) {
			throw new IllegalArgumentException("null file");
		}

		if (openFlags == null) {
			throw new IllegalArgumentException("null openFlags");
		}

		log.info("file:{}", file);
		log.info("openFlags:{}", openFlags);

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), getIRODSAccount());
		try {
			return new IRODSFileOutputStream(file, fileIOOperations, openFlags, coordinated);
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
	public IRODSFileOutputStream instanceIRODSFileOutputStreamWithRerouting(final IRODSFile file)
			throws NoResourceDefinedException, JargonException {

		log.info("instanceIRODSFileOutputStreamWithRerouting(final IRODSFile file) ");

		if (file == null) {
			throw new IllegalArgumentException("null file");
		}

		try {
			if (!file.exists()) {
				log.info("file does not exist, a new one will be created");
			}

			IRODSAccount useThisAccount = getIRODSAccount();

			boolean reroute = false;

			if (getIRODSServerProperties().isSupportsConnectionRerouting()) {
				log.info("redirects are available, check to see if I need to redirect to a resource server");
				DataObjectAO dataObjectAO = getIRODSAccessObjectFactory().getDataObjectAO(getIRODSAccount());
				String detectedHost = dataObjectAO.getHostForPutOperation(file.getAbsolutePath(), file.getResource());

				if (detectedHost == null || detectedHost.equals(FileCatalogObjectAOImpl.USE_THIS_ADDRESS)) {
					log.info("using given resource connection");
				} else if (detectedHost.equals("localhost")) {
					log.warn("localhost received as detected host, ignore and do not reroute");
				} else {
					useThisAccount = IRODSAccount.instanceForReroutedHost(getIRODSAccount(), detectedHost);
					reroute = true;
				}
			}

			FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), useThisAccount);

			if (reroute) {
				IRODSFileFactory rerouteFileFactory = getIRODSAccessObjectFactory().getIRODSFileFactory(useThisAccount);
				IRODSFile irodsFile = rerouteFileFactory.instanceIRODSFile(file.getAbsolutePath());
				return new SessionClosingIRODSFileOutputStream(irodsFile, fileIOOperations);
			} else {
				IRODSFile irodsFile = instanceIRODSFile(file.getAbsolutePath());
				return new IRODSFileOutputStream(irodsFile, fileIOOperations, OpenFlags.WRITE);
			}
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating output stream", e);
			throw new JargonException(e);
		} finally {

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
	public SessionClosingIRODSFileOutputStream instanceSessionClosingIRODSFileOutputStream(final IRODSFile file)
			throws NoResourceDefinedException, JargonException {

		log.info("instanceSessionClosingIRODSFileOutputStream");

		if (file == null) {
			throw new IllegalArgumentException("null irodsFile");
		}

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), getIRODSAccount());
		try {
			if (!file.exists()) {
				log.info("file does not exist, creating a new file");
				file.createNewFileCheckNoResourceFound(OpenFlags.READ_WRITE);
			}

			/*
			 * else if (!file.canWrite()) {
			 * log.info("this file is not writeable by the current user {}",
			 * file.getAbsolutePath()); throw new JargonException("file is not writeable:" +
			 * file.getAbsolutePath()); }
			 */

			return new SessionClosingIRODSFileOutputStream(file, fileIOOperations);
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating output stream", e);
			throw new JargonException(e);
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

		log.info("instanceIRODSFileOutputStream()");

		return instanceIRODSFileOutputStream(name, OpenFlags.WRITE);
	}

	// FIXME: instanceIRODSFileOutputStream(final String name, final String
	// replicaToken)

	@Override
	public IRODSFileOutputStream instanceIRODSFileOutputStream(final String name, final OpenFlags openFlags)
			throws NoResourceDefinedException, JargonException {

		log.info("instanceIRODSFileOutputStream()");

		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("null or empty name");
		}

		if (openFlags == null) {
			throw new IllegalArgumentException("null openFlags");
		}

		log.info("name:{}", name);
		log.info("openFlags:{}", openFlags);

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), getIRODSAccount());
		try {
			if (log.isInfoEnabled()) {
				log.info("creating IRODSFileImpl for:" + name);
			}
			IRODSFile irodsFile = instanceIRODSFile(name);
			return new IRODSFileOutputStream(irodsFile, fileIOOperations, openFlags);
		} catch (FileNotFoundException e) {
			log.error("FileNotFound creating output stream", e);
			throw new JargonException(e);
		} finally {
			/*
			 * if (this.isInstrumented()) { stopWatch.stop(); }
			 */
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFileWriter
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
			irodsFile.createNewFileCheckNoResourceFound(OpenFlags.READ_WRITE);
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
	 * @see org.irods.jargon.core.pub.io.IRODSFileFactory#instanceIRODSFileReader
	 * (java.lang.String)
	 */
	@Override
	public IRODSFileReader instanceIRODSFileReader(final String irodsFileAbsolutePath) throws JargonException {

		try {
			if (log.isInfoEnabled()) {
				log.info("creating IRODSFileReader for:" + irodsFileAbsolutePath);
			}
			IRODSFile irodsFile = instanceIRODSFile(irodsFileAbsolutePath);
			// must exist and be a file

			if (!irodsFile.exists()) {
				throw new JargonException("file does not exist in iRODS");
			}

			if (!irodsFile.isFile()) {
				throw new JargonException("the given file is not a file in iRODS");
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
	public IRODSFileInputStream instanceIRODSFileInputStream(final IRODSFile file) throws JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), getIRODSAccount());
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
	 * instanceIRODSFileInputStreamGivingFD (org.irods.jargon.core.pub.io.IRODSFile,
	 * int)
	 */
	@Override
	public IRODSFileInputStream instanceIRODSFileInputStreamGivingFD(final IRODSFile file, final int fd)
			throws JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), getIRODSAccount());
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
	public SessionClosingIRODSFileInputStream instanceSessionClosingIRODSFileInputStream(final String name)
			throws NoResourceDefinedException, JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), getIRODSAccount());
		try {
			if (log.isInfoEnabled()) {
				log.info("opening IRODSFileImpl for:" + name);
			}
			IRODSFile irodsFile = instanceIRODSFile(name);

			return new SessionClosingIRODSFileInputStream(irodsFile, fileIOOperations);
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
	public SessionClosingIRODSFileInputStream instanceSessionClosingIRODSFileInputStream(final IRODSFile file)
			throws JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), getIRODSAccount());
		try {
			return new SessionClosingIRODSFileInputStream(file, fileIOOperations);
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
	public IRODSFileInputStream instanceIRODSFileInputStream(final String name) throws JargonException {

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), getIRODSAccount());
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
	public IRODSFileInputStream instanceIRODSFileInputStreamWithRerouting(final String irodsAbsolutePath)
			throws JargonException {

		IRODSAccount useThisAccount = getIRODSAccount();
		boolean reroute = false;

		if (getIRODSServerProperties().isSupportsConnectionRerouting()) {
			log.info("redirects are available, check to see if I need to redirect to a resource server");
			DataObjectAO dataObjectAO = getIRODSAccessObjectFactory().getDataObjectAO(getIRODSAccount());
			String detectedHost = dataObjectAO.getHostForGetOperation(irodsAbsolutePath, "");

			if (detectedHost == null || detectedHost.equals(FileCatalogObjectAOImpl.USE_THIS_ADDRESS)) {
				log.info("using given resource connection");
			} else {
				useThisAccount = IRODSAccount.instanceForReroutedHost(getIRODSAccount(), detectedHost);
				reroute = true;
			}
		}

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), useThisAccount);

		try {

			log.info("opening IRODSFileImpl for:{}", irodsAbsolutePath);

			if (reroute) {
				IRODSFileFactory rerouteFileFactory = getIRODSAccessObjectFactory().getIRODSFileFactory(useThisAccount);
				IRODSFile irodsFile = rerouteFileFactory.instanceIRODSFile(irodsAbsolutePath);
				return new SessionClosingIRODSFileInputStream(irodsFile, fileIOOperations);
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
		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), getIRODSAccount());
		log.info("opening IRODSFileImpl for: {}", name);
		IRODSFile irodsFile = instanceIRODSFile(name);

		if (!irodsFile.exists()) {
			log.info("requested file does not exist, will be created");

			irodsFile.createNewFileCheckNoResourceFound(OpenFlags.READ_WRITE);
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
	 * (java.lang.String, org.irods.jargon.core.packinstr.DataObjInp.OpenFlags)
	 */
	@Override
	public IRODSRandomAccessFile instanceIRODSRandomAccessFile(final String name, final OpenFlags openFlags)
			throws NoResourceDefinedException, JargonException {

		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("null or empty name");
		}

		if (openFlags == null) {
			throw new IllegalArgumentException("null openFlags");
		}

		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), getIRODSAccount());
		log.info("opening IRODSFileImpl for: {}", name);
		IRODSFile irodsFile = instanceIRODSFile(name);

		// open the file if it is not opened
		irodsFile.open(openFlags);
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
	public IRODSRandomAccessFile instanceIRODSRandomAccessFile(final IRODSFile irodsFile)
			throws NoResourceDefinedException, JargonException {
		FileIOOperations fileIOOperations = new FileIOOperationsAOImpl(getIRODSSession(), getIRODSAccount());
		log.info("opening IRODSFileImpl for: {}", irodsFile.getAbsoluteFile());

		if (!irodsFile.exists()) {
			log.info("requested file does not exist, will be created");

			irodsFile.createNewFileCheckNoResourceFound(OpenFlags.READ_WRITE);
		}

		// open the file if it is not opened
		irodsFile.open();
		return new IRODSRandomAccessFile(irodsFile, fileIOOperations);
	}

}
