/**
 * 
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * Translation of a FileReadInp_PI operation into XML protocol format. Object is
 * immutable and thread-safe.
 * <p/>
 * This packing instruction is used to read an input stream using the
 * rcStreamRead function in iRODS.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class FileReadInp extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "fileReadInp_PI";
	public static final int FILE_READ_API_NBR = 691;
	public static final String FILE_INX = "fileInx";
	public static final String LEN = "len";

	private final int fileDescriptor;
	private final long length;

	/**
	 * Static initializer creates a version of the packing instruction suitable
	 * for reading the stream data
	 * 
	 * @param fileDescriptor
	 *            <code>int</code> with the file descriptor bound to the stream
	 *            in iRODS.
	 * @param length
	 *            <code>long</code> with the length of the stream data to be
	 *            read
	 * @return <code>FileReadInp</code> instance
	 */
	public static final FileReadInp instanceForReadStream(
			final int fileDescriptor, final long length) throws JargonException {
		return new FileReadInp(FILE_READ_API_NBR, fileDescriptor, length);
	}

	/**
	 * Private constructor.
	 * 
	 * @param apiNumber
	 *            <code>int</code> with the appropriate API number for this
	 *            invocation
	 * @param fileDescriptor
	 *            <code>int</code> with the file descriptor bound to the stream
	 *            in iRODS.
	 * @param length
	 *            <code>long</code> with the length of the stream data to be
	 *            read
	 */
	private FileReadInp(final int apiNumber, final int fileDescriptor,
			final long length) {
		super();

		if (fileDescriptor <= 0) {
			throw new IllegalArgumentException(
					"fileDescriptor is less than or equal to zero");
		}

		if (length <= 0) {
			throw new IllegalArgumentException(
					"length is less than or equal to zero");
		}

		this.fileDescriptor = fileDescriptor;
		this.length = length;
		setApiNumber(apiNumber);

	}

	@Override
	public Tag getTagValue() throws JargonException {
		Tag message = new Tag(PI_TAG, new Tag[] {
				new Tag(FILE_INX, fileDescriptor), new Tag(LEN, length) });
		return message;
	}

}
