/**
 *
 */
package org.irods.jargon.datautils.filesampler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileReader;
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
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
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
			inputStream = new BufferedInputStream(getIrodsAccessObjectFactory()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.filesampler.FileSamplerService#
	 * convertFileContentsToString(java.lang.String, long)
	 */
	@Override
	public String convertFileContentsToString(final String irodsAbsolutePath,
			final long maxSizeInKb) throws FileNotFoundException,
			FileTooLargeException, JargonException {

		log.info("convertFileContentsToString()");
		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("maxSizeInKb:{}", maxSizeInKb);

		IRODSFileFactory irodsFileFactory = getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount());

		IRODSFile irodsFile = irodsFileFactory
				.instanceIRODSFile(irodsAbsolutePath);
		if (!irodsFile.exists()) {
			log.error("did not find file at path:{}", irodsAbsolutePath);
			throw new FileNotFoundException("file not found");
		}

		if (!irodsFile.isFile()) {
			log.error("{} not a file", irodsFile);
			throw new JargonException("file is a directory");
		}

		if (maxSizeInKb > 0) {
			long lenInBytes = maxSizeInKb * 1024;
			log.info("file max in bytes:{}", lenInBytes);
			if (lenInBytes > irodsFile.length()) {
				log.info("file is too large:{} bytes", irodsFile.length());
			}
			throw new FileTooLargeException("file is too large to convert");
		}

		IRODSFileReader irodsFileReader = irodsFileFactory
				.instanceIRODSFileReader(irodsAbsolutePath);

		StringWriter writer = null;
		String fileAsString = null;

		try {
			writer = new StringWriter();
			char[] buff = new char[1024];
			int i = 0;
			while ((i = irodsFileReader.read(buff)) > -1) {
				writer.write(buff, 0, i);
			}

			fileAsString = writer.toString();
			return fileAsString;

		} catch (IOException ioe) {
			log.error("io exception reading file", ioe);
			throw new JargonException("error readingfile", ioe);
		} finally {
			try {
				irodsFileReader.close();
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				// ignore
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.datautils.filesampler.FileSamplerService#saveStringToFile
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public void saveStringToFile(String data, String irodsAbsolutePath)
			throws JargonException {

		log.info("saveStringToFile()");
		if (data == null || data.isEmpty()) {
			throw new IllegalArgumentException("null or empty data");
		}

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		log.info("data length:{}", data.length());

		IRODSFile irodsFile = getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount()).instanceIRODSFile(
						irodsAbsolutePath);

		Stream2StreamAO stream2StreamAO = getIrodsAccessObjectFactory()
				.getStream2StreamAO(getIrodsAccount());
		try {
			stream2StreamAO.streamBytesToIRODSFile(data
					.getBytes(getIrodsAccessObjectFactory()
							.getJargonProperties().getEncoding()), irodsFile);
		} catch (UnsupportedEncodingException e) {
			log.error("unsupported encoding streaming to file", e);
			throw new JargonException("error writing  file", e);
		}

		log.info("data stored");

	}
}
