/**
 *
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * Immutable object gives translation of an ExecCmd operation into XML in order
 * to close a stream opened by ExecCmd. For simplicity, this variant of the
 * packing instruction is factored into its own class.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */

public final class ExecCmdStreamClose extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "fileCloseInp_PI";
	public static final String FILE_INX = "fileInx";

	public static final int STREAM_CLOSE_API_NBR = 693;

	private final int fileDescriptor;

	/**
	 * Create an instance of the packing instruction to close the given stream.
	 *
	 * @param fileDescriptor
	 *            <code>int</code> with the file descriptor representing the
	 *            stream to close
	 * @return <code>ExecCmdStreamClose</code> instance.
	 * @throws JargonException
	 */
	public static ExecCmdStreamClose instance(final int fileDescriptor)
			throws JargonException {
		return new ExecCmdStreamClose(STREAM_CLOSE_API_NBR, fileDescriptor);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ExecCmd stream close");
		sb.append("\n  fileDescriptor:");
		sb.append(fileDescriptor);
		return sb.toString();
	}

	/**
	 * Constructor for a remote execution service close stream packing
	 * instruction call.
	 *
	 * @param apiNumber
	 *            <code>int</code> with the api number to use with this call.
	 * @param fileDescriptor
	 *            <code>int</code> with the file descriptor representing the
	 *            stream to close
	 * @throws JargonException
	 */
	private ExecCmdStreamClose(final int apiNumber, final int fileDescriptor)
			throws JargonException {

		super();

		if (fileDescriptor < 1) {
			throw new IllegalArgumentException(
					"file descriptor is 0 or negative");
		}

		this.fileDescriptor = fileDescriptor;
		setApiNumber(apiNumber);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getTagValue
	 * ()
	 */
	@Override
	public Tag getTagValue() throws JargonException {

		Tag message = new Tag(PI_TAG, new Tag[] { new Tag(FILE_INX,
				fileDescriptor) });

		return message;
	}

}
