/**
 *
 */
package org.irods.jargon.core.pub.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * iRODS-specific implementation of the {@code java.io.Reader}.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSFileReader extends Reader {

	private final transient IRODSFileInputStream irodsFileInputStream;
	public static Logger log = LoggerFactory.getLogger(IRODSFileReader.class);
	private final String connectionEncoding;

	/**
	 * iRODS-specific implementation of the {@code java.io.FileReader}. Notably this
	 * class will do character conversions to the given encoding from the binary
	 * stream data.
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} that will be the source of the stream
	 * @param irodsFileFactory
	 *            {@link IRODSFileFactory} that can be used to create various Jargon
	 *            implementations of {@code java.io.*} classes.
	 * @throws IOException
	 *             for any i/o error
	 */
	public IRODSFileReader(final IRODSFile irodsFile, final IRODSFileFactory irodsFileFactory) throws IOException {
		super();

		if (irodsFile == null) {
			throw new JargonRuntimeException("irodsFile Is null");
		}

		if (irodsFileFactory == null) {
			throw new JargonRuntimeException("irodsFileFactory is null");
		}

		try {
			irodsFileInputStream = irodsFileFactory.instanceIRODSFileInputStream(irodsFile);
		} catch (JargonException e) {
			throw new IOException("unable to open IRODSFileInputStream for:" + irodsFile.getAbsolutePath());
		}

		connectionEncoding = irodsFileInputStream.getFileIOOperations().getIRODSSession()
				.buildPipelineConfigurationBasedOnJargonProperties().getDefaultEncoding();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Reader#close()
	 */
	@Override
	public void close() throws IOException {
		log.info("closing irodsFileReader");
		irodsFileInputStream.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Reader#read(char[], int, int)
	 */
	@Override
	public int read(final char[] cbuf, final int off, final int len) throws IOException {

		byte[] b = new byte[cbuf.length];
		int lenFromIrods = irodsFileInputStream.read(b, 0, len);
		log.debug("reader acutally read {} bytes from iRODS", lenFromIrods);

		if (lenFromIrods == -1) {
			return -1;
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(b, 0, lenFromIrods);

		final InputStreamReader isr = new InputStreamReader(bais, connectionEncoding);

		int dataRead = isr.read(cbuf, off, len);
		log.debug("after decoding returning length {}", dataRead);
		return dataRead;
	}
}
