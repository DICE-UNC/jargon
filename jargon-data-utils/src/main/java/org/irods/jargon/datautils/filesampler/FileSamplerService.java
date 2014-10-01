package org.irods.jargon.datautils.filesampler;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;

/**
 * Interface for a service to sample files for previews or file format
 * recogntion or other tasks
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
	 *             if the file does not exist
	 * @throws JargonException
	 */
	byte[] sampleToByteArray(String irodsAbsolutePath, int sampleSize)
			throws FileNotFoundException, JargonException;

}