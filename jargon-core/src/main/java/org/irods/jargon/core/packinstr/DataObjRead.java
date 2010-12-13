/**
 * 
 */
package org.irods.jargon.core.packinstr;

import static edu.sdsc.grid.io.irods.IRODSConstants.dataObjReadInp_PI;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sdsc.grid.io.irods.Tag;

/**
 * Translation of a DataObjRead operation into XML protocol format.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DataObjRead extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "DataObjRead_PI";
	public static final String CREATE_MODE = "createMode";
	public static final String L1DESCINX = "l1descInx";
	public static final String LEN = "len";

	public static final int READ_FILE_API_NBR = 603;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	private final int fileDescriptor;
	private final long length;

	public static final DataObjRead instance(final int fileDescriptor,
			final long length) throws JargonException {
		return new DataObjRead(fileDescriptor, length);
	}

	private DataObjRead(final int fileDescriptor, final long length)
			throws JargonException {
		super();

		if (fileDescriptor < 1) {
			throw new JargonException("invalid fileDescriptor:"
					+ fileDescriptor);
		}

		if (length < 1) {
			throw new JargonException("zero or negative read length");
		}

		this.fileDescriptor = fileDescriptor;
		this.length = length;
		this.setApiNumber(READ_FILE_API_NBR);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getParsedTags
	 * ()
	 */
	@Override
	public String getParsedTags() throws JargonException {

		Tag message = getTagValue();

		String tagOut = message.parseTag();

		if (log.isDebugEnabled()) {
			log.debug("tag created:" + tagOut);
		}

		return tagOut;

	}

	/*
	 * 
	 * 16237 [main] INFO edu.sdsc.grid.io.irods.IRODSConnection readMessage 632-
	 * 2 millisecs 16237 [main] DEBUG edu.sdsc.grid.io.irods.IRODSCommands
	 * irodsFunction 704- <dataObjReadInp_PI><l1descInx>3</l1descInx>
	 * <len>1</len> </dataObjReadInp_PI> (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getTagValue
	 * ()
	 */

	@Override
	public Tag getTagValue() throws JargonException {

		// shim code for Bug 40 - IRODSCommands.fileRead() with length of 0
		// causes null message from irods, so it's set to 1 and unused
		Tag message = new Tag(dataObjReadInp_PI, new Tag[] {
				new Tag(L1DESCINX, fileDescriptor), new Tag(LEN, length), });

		return message;
	}

	protected int getFileDescriptor() {
		return fileDescriptor;
	}

	public long getLength() {
		return length;
	}
}
