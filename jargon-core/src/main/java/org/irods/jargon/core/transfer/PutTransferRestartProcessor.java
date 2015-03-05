/**
 *
 */
package org.irods.jargon.core.transfer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.packinstr.DataObjInp.OpenFlags;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.io.IRODSRandomAccessFile;
import org.irods.jargon.core.transfer.FileRestartInfo.RestartType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE
 */
public class PutTransferRestartProcessor extends
		AbstractTransferRestartProcessor {

	private static Logger log = LoggerFactory
			.getLogger(PutTransferRestartProcessor.class);

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 * @param restartManager
	 */
	public PutTransferRestartProcessor(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount,
			final AbstractRestartManager restartManager) {
		super(irodsAccessObjectFactory, irodsAccount, restartManager);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.transfer.AbstractTransferRestartProcessor#
	 * restartIfNecessary()
	 */
	@Override
	public void restartIfNecessary(final String irodsAbsolutePath)
			throws JargonException {
		log.info("restartIfNecessary()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		if (this.getIrodsAccessObjectFactory().getJargonProperties()
				.isLongTransferRestart()) {
			if (this.getRestartManager() == null) {
				throw new JargonRuntimeException(
						"retart manager not configured");
			}
		}

		FileRestartInfoIdentifier fileRestartInfoIdentifier = new FileRestartInfoIdentifier();
		fileRestartInfoIdentifier.setAbsolutePath(irodsAbsolutePath);
		fileRestartInfoIdentifier.setIrodsAccountIdentifier(getIrodsAccount()
				.toString());
		fileRestartInfoIdentifier.setRestartType(RestartType.PUT);

		log.info("see if restart for:{}", fileRestartInfoIdentifier);

		FileRestartInfo fileRestartInfo = getRestartManager().retrieveRestart(
				fileRestartInfoIdentifier);
		if (fileRestartInfo == null) {
			log.info("no restart");
			return;
		}

		// has restart...go ahead

		IRODSRandomAccessFile irodsRandomAccessFile = getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount())
				.instanceIRODSRandomAccessFile(irodsAbsolutePath,
						OpenFlags.READ_WRITE_CREATE_IF_NOT_EXISTS);

		InputStream localFileInputStream;
		File localFile = null;
		try {
			localFile = localFileAsFileAndCheckExists(fileRestartInfo);
			localFileInputStream = new BufferedInputStream(new FileInputStream(
					localFile));
		} catch (FileNotFoundException e) {
			log.error("file not found exception with localFile:{}", localFile,
					e);
			throw new JargonException(e);
		}

		// now put each segment
		byte[] buffer = new byte[getIrodsAccessObjectFactory()
				.getJargonProperties().getPutBufferSize()];

		long currentOffset = 0L;
		long gap;
		long lengthToUpdate;
		FileRestartDataSegment segment = null;
		for (int i = 0; i < fileRestartInfo.getFileRestartDataSegments().size(); i++) {
			log.info("process segment:{}", segment);
			gap = fileRestartInfo.getFileRestartDataSegments().get(i)
					.getOffset()
					- currentOffset;
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
					putSegment(gap, localFileInputStream, buffer,
							fileRestartInfo, segment, i, lengthToUpdate,
							irodsRandomAccessFile);

				}

			}
		}

		log.info("restart completed..remove from the cache");
		this.getRestartManager().deleteRestart(fileRestartInfoIdentifier);
		log.info("removed restart");

	}

	private void putSegment(final long gap,
			final InputStream localFileInputStream, final byte[] buffer,
			final FileRestartInfo fileRestartInfo,
			final FileRestartDataSegment segment,
			final int indexOfCurrentSegment, final long lengthToUpdate,
			final IRODSRandomAccessFile irodsRandomAccessFile)
			throws JargonException {

		long myGap = gap;
		long writtenSinceUpdated = 0L;
		long myLengthToUpdate = lengthToUpdate;
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
				amountRead = localFileInputStream.read(buffer, 0, toRead);
				irodsRandomAccessFile.write(buffer, 0, amountRead);
				myGap -= amountRead;
				writtenSinceUpdated += amountRead;
				myLengthToUpdate += amountRead;

				if (writtenSinceUpdated >= AbstractTransferRestartProcessor.RESTART_FILE_UPDATE_SIZE) {
					log.info("need to update restart");
					segment.setLength(myLengthToUpdate);
					this.getRestartManager().storeRestart(fileRestartInfo);
					writtenSinceUpdated = 0;
				}

			} catch (IOException e) {
				log.error("IOException reading local file", e);
				throw new JargonException("IO Exception reading local file", e);
			}
		}
	}
}
