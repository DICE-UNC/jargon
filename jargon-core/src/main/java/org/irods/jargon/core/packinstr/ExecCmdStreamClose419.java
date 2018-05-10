/**
 *
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * Immutable object gives translation of an ExecCmd operation into XML in order
 * to close a stream opened by ExecCmd. For simplicity, this variant of the
 * packing instruction is factored into its own class.
 * <p>
 * This version is for iRODS 4.1.9 or later
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */

public final class ExecCmdStreamClose419 extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "fileCloseInp_PI";
	public static final String FILE_INX = "fileInx";
	public static final String IN_PDMO = "in_pdmo";
	public static final int STREAM_CLOSE_API_NBR = 693;
	private final String pdmo;
	private final int fileDescriptor;

	/**
	 * Create an instance of the packing instruction to close the given stream.
	 *
	 * @param fileDescriptor
	 *            {@code int} with the file descriptor representing the stream to
	 *            close
	 * @param pdmo
	 *            {@code String} with the post disconnect mx operation to carry out.
	 *            Blank if not used.
	 * @return {@link ExecCmdStreamClose419} instance.
	 * @throws JargonException
	 *             for iRODS error
	 */
	public static ExecCmdStreamClose419 instance(final int fileDescriptor, final String pdmo) throws JargonException {
		return new ExecCmdStreamClose419(STREAM_CLOSE_API_NBR, fileDescriptor, pdmo);
	}

	/**
	 * Constructor for a remote execution service close stream packing instruction
	 * call.
	 *
	 * @param apiNumber
	 *            {@code int} with the api number to use with this call.
	 * @param fileDescriptor
	 *            {@code int} with the file descriptor representing the stream to
	 *            close
	 * @param pdmo
	 *            {@code String} with the post disconnect mx operation to carry out.
	 *            Blank if not used.
	 * @throws JargonException
	 *             for iRODS error
	 */
	private ExecCmdStreamClose419(final int apiNumber, final int fileDescriptor, final String pdmo)
			throws JargonException {

		super();

		if (fileDescriptor < 1) {
			throw new IllegalArgumentException("file descriptor is 0 or negative");
		}

		if (pdmo == null) {
			throw new IllegalArgumentException("pdmo is null");
		}

		this.fileDescriptor = fileDescriptor;
		this.pdmo = pdmo;
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

		Tag message = new Tag(PI_TAG, new Tag[] { new Tag(FILE_INX, fileDescriptor), new Tag(IN_PDMO, pdmo) });

		return message;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ExecCmdStreamClose419 [");
		if (pdmo != null) {
			builder.append("pdmo=").append(pdmo).append(", ");
		}
		builder.append("fileDescriptor=").append(fileDescriptor).append("]");
		return builder.toString();
	}

}
