package org.irods.jargon.ticket;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OverwriteException;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;
import org.irods.jargon.ticket.io.CleanUpWhenClosedInputStream;
import org.irods.jargon.ticket.io.FileStreamAndInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client transfer and other operations that are ticket enabled, wrapped with
 * ticket semantics
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class TicketClientOperationsImpl extends AbstractTicketService implements
		TicketClientOperations {

	public static final Logger log = LoggerFactory
			.getLogger(TicketClientOperationsImpl.class);

	private DataTransferOperations dataTransferOperations = null;
	private TicketClientSupport ticketClientSupport = null;

	/**
	 * Constructor initializes service for
	 * 
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 * @throws JargonException
	 */
	TicketClientOperationsImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) throws JargonException {

		if (irodsAccessObjectFactory == null) {
			throw new IllegalArgumentException("null irodsAccessObjectFactory");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
		this.irodsAccount = irodsAccount;

		dataTransferOperations = irodsAccessObjectFactory
				.getDataTransferOperations(irodsAccount);
		ticketClientSupport = new TicketClientSupport(irodsAccessObjectFactory,
				irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.ticket.TicketClientOperations#putFileToIRODSUsingTicket
	 * (java.lang.String, java.io.File, org.irods.jargon.core.pub.io.IRODSFile,
	 * org.irods.jargon.core.transfer.TransferStatusCallbackListener,
	 * org.irods.jargon.core.transfer.TransferControlBlock)
	 */
	@Override
	public void putFileToIRODSUsingTicket(
			final String ticketString,
			final File sourceFile,
			final IRODSFile targetIrodsFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws DataNotFoundException, OverwriteException, JargonException {

		log.info("putFileToIRODSUsingTicket()");

		if (ticketString == null || ticketString.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticketString");
		}

		// other param checks done in delegated methods

		log.info("initializing session with ticket:{}", ticketString);
		ticketClientSupport.initializeSessionWithTicket(ticketString);

		log.info("session initialized, doing put operation");
		dataTransferOperations.putOperation(sourceFile, targetIrodsFile,
				transferStatusCallbackListener, transferControlBlock);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ticket.TicketClientOperations#
	 * getOperationFromIRODSUsingTicket(java.lang.String,
	 * org.irods.jargon.core.pub.io.IRODSFile, java.io.File,
	 * org.irods.jargon.core.transfer.TransferStatusCallbackListener,
	 * org.irods.jargon.core.transfer.TransferControlBlock)
	 */
	@Override
	public void getOperationFromIRODSUsingTicket(
			final String ticketString,
			final IRODSFile irodsSourceFile,
			final File targetLocalFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
			throws DataNotFoundException, OverwriteException, JargonException {

		log.info("getFileFromIRODSUsingTicket()");

		if (ticketString == null || ticketString.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticketString");
		}

		// other param checks done in delegated methods

		log.info("initializing session with ticket:{}", ticketString);
		ticketClientSupport.initializeSessionWithTicket(ticketString);

		log.info("session initialized, doing get operation");
		dataTransferOperations.getOperation(irodsSourceFile, targetLocalFile,
				transferStatusCallbackListener, transferControlBlock);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ticket.TicketClientOperations#
	 * redeemTicketAndStreamToIRODSCollection(java.lang.String,
	 * java.lang.String, java.lang.String, java.io.InputStream, java.io.File)
	 */
	@Override
	public void redeemTicketAndStreamToIRODSCollection(
			final String ticketString,
			final String irodsCollectionAbsolutePath, final String fileName,
			final InputStream inputStreamForFileData,
			final File temporaryCacheDirectoryLocation)
			throws DataNotFoundException, OverwriteException, JargonException {

		log.info("redeemTicketAndStreamToIRODSCollection()");

		if (ticketString == null || ticketString.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticketString");
		}

		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		if (inputStreamForFileData == null) {
			throw new IllegalArgumentException("null inputStreamForFileData");
		}

		if (temporaryCacheDirectoryLocation == null) {
			throw new IllegalArgumentException(
					"null temporaryCacheDirectoryLocation");
		}

		if (!temporaryCacheDirectoryLocation.exists()) {
			throw new JargonException(
					"temporaryCacheDirectoryLocation does not exist");
		}

		if (!temporaryCacheDirectoryLocation.isDirectory()) {
			throw new JargonException(
					"temporaryCacheDirectoryLocation is not a directory");
		}

		/*
		 * Everything is in order I need to stream the input stream data to a
		 * temporary file first
		 */

		StringBuffer sb = new StringBuffer();
		sb.append("redeemTicketAndStreamToIRODSCollection_");
		sb.append(System.currentTimeMillis());
		sb.append("_");
		sb.append(fileName);
		String tempFileName = sb.toString();
		log.info("temp file name:{}", tempFileName);
		File tempFile = new File(temporaryCacheDirectoryLocation, tempFileName);

		try {
			FileUtils.copyInputStreamToFile(inputStreamForFileData, tempFile);
		} catch (IOException e) {
			log.error("io exception copying input stream to temp file", e);
			throw new JargonException(
					"error copying provided input stream to temporary cache");
		}

		IRODSFile targetIrodsFile = getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount()).instanceIRODSFile(
						irodsCollectionAbsolutePath, fileName);
		log.info("target iRODS file:{}", targetIrodsFile);

		log.info("data has been copied to temp file, now put to iRODS via ticket");
		/*
		 * Put file, try and clean up no matter what happens
		 */
		try {
			putFileToIRODSUsingTicket(ticketString, tempFile, targetIrodsFile,
					null, null);
		} finally {
			log.info("delete the temp file");
			tempFile.delete();
			log.info("close input stream");
			try {
				inputStreamForFileData.close();
			} catch (IOException e) {
			}
		}

		log.info("transfer complete");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.ticket.TicketClientOperations#
	 * redeemTicketGetDataObjectAndStreamBack(java.lang.String,
	 * org.irods.jargon.core.pub.io.IRODSFile, java.io.File)
	 */
	@Override
	public FileStreamAndInfo redeemTicketGetDataObjectAndStreamBack(
			final String ticketString, final IRODSFile irodsSourceFile,
			final File intermediateCacheRootDirectory)
			throws DataNotFoundException, JargonException {

		log.info("redeemTicketGetDataObjectAndStreamBack()");

		if (ticketString == null || ticketString.isEmpty()) {
			throw new IllegalArgumentException("null or empty ticketString");
		}

		if (intermediateCacheRootDirectory == null) {
			throw new IllegalArgumentException(
					"null intermediateCacheRootDirectory");
		}

		log.info("intermediate cache root dir:{}",
				intermediateCacheRootDirectory.getAbsolutePath());

		if (!intermediateCacheRootDirectory.exists()) {
			throw new JargonException(
					"cannot create intermediate cache, root dir does not exist");
		}

		// compute file name under cache dir as current time +
		// irodsSourceFileName
		StringBuilder sb = new StringBuilder();
		sb.append(".tempTicketFileCache_");
		sb.append(System.currentTimeMillis());
		sb.append("_");
		sb.append(irodsSourceFile.getName());
		String tempFileName = sb.toString();

		log.info("temp file name: {}", tempFileName);

		File intermediateCacheFile = new File(intermediateCacheRootDirectory,
				tempFileName);

		// other param checks done in delegated methods

		log.info("initializing session with ticket:{}", ticketString);
		ticketClientSupport.initializeSessionWithTicket(ticketString);

		log.info("session initialized, doing get operation");
		dataTransferOperations.getOperation(irodsSourceFile,
				intermediateCacheFile, null, null);
		log.info("file obtained and in cache, now returning an input stream");
		InputStream inputStream;
		try {
			inputStream = new BufferedInputStream(
					new CleanUpWhenClosedInputStream(intermediateCacheFile));
			return new FileStreamAndInfo(inputStream,
					intermediateCacheFile.length());
		} catch (FileNotFoundException e) {
			log.error("cannot find temp cache file to stream back", e);
			throw new JargonException(
					"cannot find the temporary cache stream I had created");
		}

	}

}
