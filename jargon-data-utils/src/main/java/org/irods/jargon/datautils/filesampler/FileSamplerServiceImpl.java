/**
 * 
 */
package org.irods.jargon.datautils.filesampler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.datautils.AbstractDataUtilsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to sample a given file for previews, file format recogntion, and the
 * like
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FileSamplerServiceImpl extends AbstractDataUtilsServiceImpl
		implements FileSamplerService {

	public static final Logger log = LoggerFactory
			.getLogger(FileSamplerServiceImpl.class);

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public FileSamplerServiceImpl(
			IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/**
	 * 
	 */
	public FileSamplerServiceImpl() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.datautils.filesampler.FileSamplerService#sampleToByteArray
	 * (java.lang.String, int)
	 */
	@Override
	public byte[] sampleToByteArray(final String irodsAbsolutePath,
			final int sampleSize) throws FileNotFoundException, JargonException {

		log.info("sampleToByteArray()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null irodsAbsolutePath");
		}

		if (sampleSize <= 0) {
			throw new IllegalArgumentException("sample size must be > 0");
		}

		log.info("absolutePath:{}", irodsAbsolutePath);
		log.info("sampleSize:{}", sampleSize);

		if (sampleSize > MAX_SAMPLE_SIZE) {
			throw new IllegalArgumentException(
					"sample size too large,use an input stream");
		}

		log.info("getting input stream...");
		InputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream(this
					.getIrodsAccessObjectFactory()
					.getIRODSFileFactory(getIrodsAccount())
					.instanceIRODSFileInputStream(irodsAbsolutePath));

			byte[] sample = IOUtils.toByteArray(inputStream, sampleSize);
			log.info("done...");
			return sample;

		} catch (IOException e) {
			log.error("IOException reading from stream", e);
			throw new JargonException("io exception reading sample", e);
		} finally {
			if (inputStream != null) {
				IOUtils.closeQuietly(inputStream);
			}
		}

	}

}
