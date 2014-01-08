/**
 * 
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translation of a DataObjWriteInp operation into XML protocol format.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataObjWriteInp extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "dataObjWriteInp_PI";
	public static final String DATA_OBJ_INX = "dataObjInx";
	public static final String LEN = "len";

	public static final int WRITE_API_NBR = 676;
	private static Logger log = LoggerFactory.getLogger(DataObjWriteInp.class);

	private final int fileDescriptor;
	private final long length;

	public static final DataObjWriteInp instance(final int fileDescriptor,
			final long length) throws JargonException {
		return new DataObjWriteInp(fileDescriptor, length);
	}

	private DataObjWriteInp(final int fileDescriptor, final long length)
			throws JargonException {
		super();

		if (fileDescriptor <= 0) {
			throw new JargonException(
					"missing file descriptor, is the file open?");
		}

		if (length <= 0) {
			throw new JargonException("attempting to write a 0 length buffer");
		}
		this.fileDescriptor = fileDescriptor;
		this.length = length;

	}

	@Override
	public String getParsedTags() throws JargonException {

		Tag message = getTagValue();

		String tagOut = message.parseTag();

		if (log.isDebugEnabled()) {
			log.debug("tag created:" + tagOut);
		}

		return tagOut;

	}

	@Override
	public Tag getTagValue() throws JargonException {
		Tag message = new Tag(PI_TAG, new Tag[] {
				new Tag(DATA_OBJ_INX, getFileDescriptor()),
				new Tag(LEN, (int) getLength()), });

		return message;
	}

	protected int getFileDescriptor() {
		return fileDescriptor;
	}

	protected long getLength() {
		return length;
	}

}
