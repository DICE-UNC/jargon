package org.irods.jargon.datautils.filesampler;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;

/**
 * Interface for a service to sample files for previews or file format
 * recognition or other tasks
 *
 * @author Mike Conway - DICE
 *
 */
public interface FileSamplerService {

	public static final int MAX_SAMPLE_SIZE = 20 * 1024;

	/**
	 * Sample a file and return the first n bytes based on the
	 * <code>sampleSize</code> up to the <code>MAX_SAMPLE_SIZE</code>
	 *
	 * @param irodsAbsolutePath
	 *            <code>String</code> with absolute path to the iRODS file
	 * @param sampleSize
	 *            <code>int</code> with a sample size up to the
	 *            <code>MAX_SAMPLE_SIZE</code>
	 * @return <code>byte[]</code> with the sample of the file
	 * @throws FileNotFoundException
	 *             {@link FileNotFoundException}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	byte[] sampleToByteArray(String irodsAbsolutePath, int sampleSize) throws FileNotFoundException, JargonException;

	/**
	 * Convert the content of the given data object to a String
	 *
	 * @param irodsAbsolutePath
	 *            <code>String</code> with absolute path to the iRODS file
	 * @param maxSizeInKb
	 *            <code>long</code> with the maximum file length (in kb) to convert,
	 *            if it is too long an exception will be thrown. A 0 can be entered
	 *            which will ignore maximums.
	 * @return <code>String</code> with the file contents. The method does NOT check
	 *         the file to see if it makes sense to try and return as String data
	 * @throws FileNotFoundException
	 *             {@link FileNotFoundException}
	 * @throws FileTooLargeException
	 *             {@link FileTooLargeException}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	String convertFileContentsToString(String irodsAbsolutePath, long maxSizeInKb)
			throws FileNotFoundException, FileTooLargeException, JargonException;

	/**
	 * Write (and overwrite) the contents of the given string to the iRODS file
	 *
	 * @param data
	 *            <code>String</code> the data to write to iRODS
	 * @param irodsAbsolutePath
	 *            <code>String</code> of the absolute path in iRODS where the file
	 *            should be written.
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	void saveStringToFile(String data, String irodsAbsolutePath) throws JargonException;

}