/**
 *
 */
package org.irods.jargon.core.transfer;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - (DICE) Restart processor abstract superclass. This
 *         defines a service that can restart a get or put transfer within a
 *         file.
 *
 */
public abstract class AbstractTransferRestartProcessor extends
		AbstractJargonService {

	private final AbstractRestartManager restartManager;
	private static Logger log = LoggerFactory
			.getLogger(AbstractTransferRestartProcessor.class);
	private final TransferStatusCallbackListener transferStatusCallbackListener;
	private final TransferControlBlock transferControlBlock;

	public static final long RESTART_FILE_UPDATE_SIZE = 32 * 1024 * 1024;

	public enum OpenType {
		READ, WRITE
	}

	/**
	 * Constructor with required dependencies
	 *
	 * @param irodsAccessObjectFactory
	 *            {@link IRODSAccessObjectFactory irodsAccessObjectFactory}
	 * @param irodsAccount
	 *            {@link IRODSAccount}
	 * @param restartManager
	 *            {@link AbstractRestartManager}
	 */
	public AbstractTransferRestartProcessor(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount,
			final AbstractRestartManager restartManager,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock) {
		super(irodsAccessObjectFactory, irodsAccount);
		if (restartManager == null) {
			throw new IllegalArgumentException("null restartManager");
		}
		if (transferControlBlock == null) {
			throw new IllegalArgumentException("null transferControlBlock");
		}
		this.transferControlBlock = transferControlBlock;
		this.transferStatusCallbackListener = transferStatusCallbackListener;

		this.restartManager = restartManager;
	}

	/**
	 * @return the restartManager
	 */
	public AbstractRestartManager getRestartManager() {
		return restartManager;
	}

	/**
	 * Check the need to restart the file, and do the restart processing if
	 * needed, based on the data held by the restart manager. * @throws
	 * RestartFailedException if the actual restart process failed
	 * 
	 * @throws FileRestartManagementException
	 *             if the restart failed for configuration or other reasons, and
	 *             restart should not be attempted again
	 * @throws JargonException
	 *             general exception that may trigger another restart attempt
	 */
	public abstract void restartIfNecessary(final String irodsAbsolutePath)
			throws RestartFailedException, FileRestartManagementException,
			JargonException;

	/**
	 * Given the restart info return the local file and make sure it exists
	 *
	 * @param fileRestartInfo
	 *            {@link FileRestartInfo} that describes the transfer
	 * @return {@link RandomAccessFile}
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	protected RandomAccessFile localFileAsFileAndCheckExists(
			final FileRestartInfo fileRestartInfo, final OpenType openType)
			throws FileNotFoundException, JargonException {
		log.info("localFileAsFileAndCheckExists()");

		RandomAccessFile localFile = localFileAsFile(fileRestartInfo, openType);

		return localFile;

	}

	/**
	 * Get the local file that is being operated upon
	 *
	 * @param fileRestartInfo
	 *            fileRestartInfo {@link FileRestartInfo} that describes the
	 *            transfer
	 * @param openType
	 * @return {@link RandomAccessFile} that represents the local part of the
	 *         transfer
	 * @throws JargonException
	 */
	protected RandomAccessFile localFileAsFile(
			final FileRestartInfo fileRestartInfo, final OpenType openType)
			throws JargonException {
		log.info("localFileAsFileAndCheckExists()");
		if (fileRestartInfo == null) {
			throw new IllegalArgumentException("null fileRestartInfo");
		}
		if (openType == null) {
			throw new IllegalArgumentException("null openType");
		}

		if (fileRestartInfo.getLocalAbsolutePath() == null
				|| fileRestartInfo.getLocalAbsolutePath().isEmpty()) {
			log.error("no localFilePath in restart info for:{}",
					fileRestartInfo);
			throw new JargonException(
					"unable to find a local file path in the restart info");
		}

		String openFlag;
		if (openType == OpenType.READ) {
			openFlag = "r";
		} else {
			openFlag = "rw";
		}
		try {
			return new RandomAccessFile(fileRestartInfo.getLocalAbsolutePath(),
					openFlag);
		} catch (FileNotFoundException e) {
			log.error("local file not found:{}",
					fileRestartInfo.getLocalAbsolutePath());
			throw new JargonException("cannot find local file");
		}

	}

	/**
	 * Method to retrieve the restart info from the manager, this may end up
	 * being <code>null</code>
	 *
	 * @param fileRestartInfoIdentifier
	 *            {@link FileRestartInfoIdentifier}
	 * @return {@link FileRestartInfo}
	 * @throws FileRestartManagementException
	 */
	protected FileRestartInfo retrieveFileRestartInfoForIdentifier(
			final FileRestartInfoIdentifier fileRestartInfoIdentifier)
			throws FileRestartManagementException {
		if (fileRestartInfoIdentifier == null) {
			throw new IllegalArgumentException("null fileRestartInfoIdentifier");
		}
		if (getRestartManager() == null) {
			throw new JargonRuntimeException("no restart manager configured");
		}
		return getRestartManager().retrieveRestart(fileRestartInfoIdentifier);
	}

	/**
	 * Retrieve the restart info if it exists and Jargon is configured to do
	 * restarts.
	 * <p/>
	 * This method will check the configuration as well as the actual restart
	 * manager, and will return <code>null</code>
	 *
	 *
	 * @param irodsAbsolutePath
	 * @param restartType
	 * @return {@link FileRestartInfo}
	 * @throws FileRestartManagementException
	 */
	protected FileRestartInfo retrieveRestartIfConfiguredOrNull(
			final String irodsAbsolutePath,
			final FileRestartInfo.RestartType restartType)
			throws FileRestartManagementException {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		try {
			if (getIrodsAccessObjectFactory().getJargonProperties()
					.isLongTransferRestart()) {
				if (getRestartManager() == null) {
					log.error("no restart manager configured");
					throw new FileRestartManagementException(
							"retart manager not configured");
				}
			}
		} catch (JargonException e) {
			log.error("exception accessing restart manager", e);
			throw new FileRestartManagementException("retart manager error", e);
		}

		FileRestartInfoIdentifier fileRestartInfoIdentifier = new FileRestartInfoIdentifier();
		fileRestartInfoIdentifier.setAbsolutePath(irodsAbsolutePath);
		fileRestartInfoIdentifier.setIrodsAccountIdentifier(getIrodsAccount()
				.toString());
		fileRestartInfoIdentifier.setRestartType(restartType);

		log.info("see if restart for:{}", fileRestartInfoIdentifier);

		FileRestartInfo fileRestartInfo = getRestartManager().retrieveRestart(
				fileRestartInfoIdentifier);

		return fileRestartInfo;

	}

	/**
	 * @return the transferStatusCallbackListener
	 */
	public TransferStatusCallbackListener getTransferStatusCallbackListener() {
		return transferStatusCallbackListener;
	}

	/**
	 * @return the transferControlBlock
	 */
	public TransferControlBlock getTransferControlBlock() {
		return transferControlBlock;
	}

}
