package org.irods.jargon.ticket.io;

import java.io.InputStream;

/**
 * Value object holds an
 * <code>InputStream<code> as a result of a ticket based 'get' of a file, where a stream of the file data is desired.  Note
 * that currently only 'get' and 'put' are supported via tickets, so mid-tier applications that wish to stream data back to the client need to do an 
 * intermediate get to the mid-tier platform and then stream from this location.
 * <p/>
 * Tickets are limited in what they can access, so various operations that refer to the iCAT, such as obtaining the length, or differentiating between
 * a file and a collection, cannot be done in the typical way.  As a work-around, this object holds the lenght of the cached file so that it may be 
 * sent in browser responses.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class FileStreamAndInfo {
	private final InputStream inputStream;
	private final long length;

	/**
	 * Constructor with an input stream and length of the data to be streamed
	 * 
	 * @param inputStream
	 *            {@link CleanUpWhenClosedInputStream} is returned as the
	 *            underlying stream object. This is a subclass of
	 *            <code>InputStream</code> that gets rid of the cached file on
	 *            close. Note that, when obtained through the ticket services,
	 *            this stream is already buffered
	 * @param length
	 *            <code>long</code> with the length of data to be streamed
	 */
	public FileStreamAndInfo(final InputStream inputStream, final long length) {

		if (inputStream == null) {
			throw new IllegalArgumentException("null inputStream");
		}

		if (length <= 0) {
			throw new IllegalArgumentException("length must be > 0");
		}

		this.inputStream = inputStream;
		this.length = length;
	}

	/**
	 * @return the inputStream that is pre-buffered, and will delete the
	 *         underlying cache file when closed
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * @return the length of the data to be streamed
	 */
	public long getLength() {
		return length;
	}

}
