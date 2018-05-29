/**
 *
 */
package org.irods.jargon.core.pub.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * iRODS specific implementation of a {@code java.io.Writer}
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class IRODSFileWriter extends Writer {

	private final IRODSFileOutputStream irodsFileOutputStream;
	public static Logger log = LoggerFactory.getLogger(IRODSFileWriter.class);
	private final String connectionEncoding;

	/**
	 * Create an instance of a writer for iRODS.
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} to write to.
	 * @param irodsFileFactory
	 *            {@link IRODSFileFactory} that will create the file.
	 * @throws IOException
	 *             for any iRODS error
	 */
	public IRODSFileWriter(final IRODSFile irodsFile, final IRODSFileFactory irodsFileFactory) throws IOException {
		super();

		if (irodsFile == null) {
			throw new JargonRuntimeException("irodsFile Is null");
		}

		if (irodsFileFactory == null) {
			throw new JargonRuntimeException("irodsFileFactory is null");
		}

		try {
			irodsFileOutputStream = irodsFileFactory.instanceIRODSFileOutputStream(irodsFile);
		} catch (JargonException e) {
			throw new IOException("unable to open IRODSFileOutputStream for:" + irodsFile.getAbsolutePath());
		}

		connectionEncoding = irodsFileOutputStream.getFileIOOperations().getIRODSSession()
				.buildPipelineConfigurationBasedOnJargonProperties().getDefaultEncoding();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.Writer#close()
	 */
	@Override
	public void close() throws IOException {
		log.info("closing irodsFileWriter");
		irodsFileOutputStream.close();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.Writer#flush()
	 */
	@Override
	public void flush() throws IOException {
		irodsFileOutputStream.flush();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.Writer#write(char[], int, int)
	 */
	@Override
	public void write(final char[] cbuf, final int off, final int len) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(bos, connectionEncoding);
		osw.write(cbuf, off, len);
		osw.flush();
		byte[] oswBytes = bos.toByteArray();
		log.debug("converted {} bytes", oswBytes.length);
		// chars converted, not write using file output stream
		if (oswBytes.length > 0) {
			irodsFileOutputStream.write(bos.toByteArray());
		}
	}

}
