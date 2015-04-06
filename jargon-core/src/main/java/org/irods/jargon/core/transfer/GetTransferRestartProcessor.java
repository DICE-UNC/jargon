/**
 * 
 */
package org.irods.jargon.core.transfer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.NoResourceDefinedException;
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.FileIOOperations.SeekWhenceType;
import org.irods.jargon.core.pub.io.IRODSRandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle a restart of a get operation
 * 
 * @author Mike Conway - DICE
 *
 */
public class GetTransferRestartProcessor extends
		AbstractTransferRestartProcessor {

	private static Logger log = LoggerFactory
			.getLogger(GetTransferRestartProcessor.class);

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 * @param restartManager
	 */
	public GetTransferRestartProcessor(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount,
			final AbstractRestartManager restartManager,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock) {
		super(irodsAccessObjectFactory, irodsAccount, restartManager,
				transferStatusCallbackListener, transferControlBlock);
	}

	@Override
	public void restartIfNecessary(String irodsAbsolutePath)
			throws RestartFailedException, FileRestartManagementException {

		log.info("restartIfNecessary()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		FileRestartInfo fileRestartInfo = retrieveRestartIfConfiguredOrNull(
				irodsAbsolutePath, FileRestartInfo.RestartType.GET);

		if (fileRestartInfo == null) {
			log.info("no restart");
			return;
		}

		try {
			processRestart(irodsAbsolutePath, fileRestartInfo);
		} catch (JargonException e) {
			log.error("exception accessing restart manager", e);
			throw new FileRestartManagementException("restart manager error", e);
		}

	}

	private void processRestart(String irodsAbsolutePath,
			FileRestartInfo fileRestartInfo) throws JargonException {

		log.info("processRestart()");
		log.info("get local file as rw random access file...");
		RandomAccessFile localFile = null;
		try {
			localFile = localFileAsFileAndCheckExists(fileRestartInfo,
					OpenType.WRITE);
		} catch (FileNotFoundException e) {
			log.error("local file not found", e);
			throw new RestartFailedException("local file not found", e);
		}

		log.info("open iRODS file as read only random access...");

		IRODSRandomAccessFile irodsRandomAccessFile;
		try {
			irodsRandomAccessFile = getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSRandomAccessFile(irodsAbsolutePath,
							OpenFlags.READ);
		} catch (NoResourceDefinedException e1) {
			log.error("no resource defined", e1);
			throw new RestartFailedException(
					"cannot get irodsRandomAccessFile", e1);
		} catch (JargonException e1) {
			log.error("general jargon error getting irods random file", e1);
			throw new RestartFailedException(
					"cannot get irodsRandomAccessFile", e1);
		}

		byte[] buffer;

		try {
			localFile = localFileAsFileAndCheckExists(fileRestartInfo,
					OpenType.READ);

			// now put each segment
			buffer = new byte[getIrodsAccessObjectFactory()
					.getJargonProperties().getPutBufferSize()];
			long currentOffset = 0L;
			long gap;
			long lengthToUpdate;
			FileRestartDataSegment segment = null;
			for (int i = 0; i < fileRestartInfo.getFileRestartDataSegments()
					.size(); i++) {
				segment = fileRestartInfo.getFileRestartDataSegments().get(i);
				log.info("process segment:{}", segment);
				gap = segment.getOffset() - currentOffset;
				if (gap < 0) {
					log.warn("my segment has a gap < 0..continuing:{}", segment);
				} else if (gap > 0) {

					// ok, have a gap > 0, let's get our restart on

					if (i == 0) {
						// should not be here
						lengthToUpdate = 0;
					} else {
						lengthToUpdate = fileRestartInfo
								.getFileRestartDataSegments().get(i - 1)
								.getLength();
					}

					getSegment(gap, localFile, buffer, fileRestartInfo,
							segment, i, lengthToUpdate, irodsRandomAccessFile);
					currentOffset += gap;
				}

				if (fileRestartInfo.getFileRestartDataSegments().get(i)
						.getLength() > 0) {
					currentOffset += segment.getLength();
					localFile.seek(currentOffset);
					irodsRandomAccessFile.seek(currentOffset,
							SeekWhenceType.SEEK_CURRENT);
				}
			}

			// put final segment based on file size

			gap = localFile.length() - currentOffset;
			if (gap > 0) {
				log.info("writing last segment based on file length");
				int i = fileRestartInfo.getFileRestartDataSegments().size() - 1;

				getSegment(gap, localFile, buffer, fileRestartInfo, segment, i,
						segment.getLength(), irodsRandomAccessFile);
			}

			log.info("restart completed..remove from the cache");
			this.getRestartManager().deleteRestart(
					fileRestartInfo.identifierFromThisInfo());
			log.info("removed restart");
		} catch (FileNotFoundException e) {
			log.error("file not found exception with localFile:{}", localFile,
					e);
			throw new RestartFailedException(e);
		} catch (JargonException e) {
			log.error("general jargon error getting irods random file", e);
			throw new RestartFailedException("cannot get local file", e);
		} catch (IOException e) {
			log.error("end of file exception with localFile:{}", localFile, e);
			throw new RestartFailedException(e);
		} finally {

			try {
				irodsRandomAccessFile.close();
			} catch (IOException e) {
				log.error(
						"error closing irods random access file during restart",
						e);
				throw new RestartFailedException(
						"exception closing irods restart file", e);
			}
			try {
				localFile.close();
			} catch (IOException e) {
				log.warn("error closing local file, logged and ignored", e);
			}

		}

	}

	private void getSegment(long gap, RandomAccessFile localFile,
			byte[] buffer, FileRestartInfo fileRestartInfo,
			FileRestartDataSegment segment, int i, long length,
			IRODSRandomAccessFile irodsRandomAccessFile)
			throws RestartFailedException, FileRestartManagementException {

		long myGap = gap;
		long writtenSinceUpdated = 0L;
		int toRead = 0;
		while (myGap > 0) {
			if (myGap > buffer.length) {
				toRead = buffer.length;
			} else {
				toRead = (int) myGap;
			}

			log.info("reading buffer from input file...");
			int amountRead;
			try {
				amountRead = irodsRandomAccessFile.read(buffer, 0, toRead);
				localFile.write(buffer, 0, amountRead);
				myGap -= amountRead;
				writtenSinceUpdated += amountRead;

				if (writtenSinceUpdated >= AbstractTransferRestartProcessor.RESTART_FILE_UPDATE_SIZE) {
					log.info("need to update restart");
					this.getRestartManager().updateLengthForSegment(
							fileRestartInfo.identifierFromThisInfo(),
							segment.getThreadNumber(), writtenSinceUpdated);
					writtenSinceUpdated = 0;
				}

			} catch (IOException e) {
				log.error("IOException reading local file", e);
				throw new RestartFailedException(
						"IO Exception reading local file", e);
			}
		}
	}
}
