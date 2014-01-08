/**
 * 
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * Translation of a DataObjInp operation into XML protocol format.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataObjCloseInp extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "dataObjCloseInp_PI";

	public static final String L1_DESC_INX = "l1descInx";
	public static final String BYTES_WRITTEN = "bytesWritten";
	public static final int FILE_CLOSE_API_NBR = 673;

	private final int fileDescriptor;
	private final long bytesWritten;

	public static final DataObjCloseInp instance(final int fileDescriptor,
			final long bytesWritten) throws JargonException {
		return new DataObjCloseInp(fileDescriptor, bytesWritten);
	}

	private DataObjCloseInp(final int fileDescriptor, final long bytesWritten)
			throws JargonException {
		super();

		if (fileDescriptor < 1) {
			throw new JargonException("invalid file descriptor:"
					+ fileDescriptor);
		}

		if (bytesWritten < 0L) {
			throw new JargonException("invalid bytes written value:"
					+ bytesWritten);
		}

		this.fileDescriptor = fileDescriptor;
		this.bytesWritten = bytesWritten;

	}

	public int getFileDescriptor() {
		return fileDescriptor;
	}

	public long getBytesWritten() {
		return bytesWritten;
	}

	@Override
	public Tag getTagValue() throws JargonException {
		Tag message = new Tag(PI_TAG, new Tag[] {
				new Tag(L1_DESC_INX, getFileDescriptor()),
				new Tag(BYTES_WRITTEN, getBytesWritten()) });
		return message;

	}

}
