package org.irods.jargon.ticket.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Variant of normal {@code InputStream} that will clean up the underlying
 * file when the stream is closed, otherwise, just a normal stream.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class CleanUpWhenClosedInputStream extends FileInputStream {

	private final File tempFile;

	public CleanUpWhenClosedInputStream(final File tempFile)
			throws FileNotFoundException {
		super(tempFile);
		this.tempFile = tempFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FileInputStream#close()
	 */
	@Override
	public void close() throws IOException {
		super.close();
		tempFile.delete();
	}

}
