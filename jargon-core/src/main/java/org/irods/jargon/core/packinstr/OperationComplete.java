/**
 *
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * Simple packing instruction for operation complete messages
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class OperationComplete extends AbstractIRODSPackingInstruction {

	private static final String PI_TAG = "INT_PI";
	private static final int OPR_COMPLETE_API_NBR = 626;
	private int status = 0;

	public static OperationComplete instance(final int status) {
		return new OperationComplete(status);
	}

	/**
	 * Private constructor
	 *
	 * @param status
	 *            <code>int</code> >= 0 that is the status to send with the
	 *            operation complete message
	 */
	private OperationComplete(final int status) {
		if (status < 0) {
			throw new IllegalArgumentException(
					"status must be greater than or equal to zero");
		}
		this.status = status;
		setApiNumber(OPR_COMPLETE_API_NBR);
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
		return new Tag(PI_TAG, new Tag[] { new Tag("myInt", status), });
	}

}
