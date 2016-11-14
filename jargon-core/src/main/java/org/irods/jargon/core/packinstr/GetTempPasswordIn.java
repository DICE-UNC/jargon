package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * Packing instruction to request a temporary password.
 *
 * @author Mike Conway - DICE (www.irods.org)
 */
public class GetTempPasswordIn extends AbstractIRODSPackingInstruction {

	public static final int GET_TEMP_PASSWORD_API_NBR = 709;

	/**
	 * Instance method creates a request to generate a temporary password.
	 *
	 * @return <code>GetTempPasswordIn</code> instance
	 */
	public static GetTempPasswordIn instance() {
		return new GetTempPasswordIn();
	}

	private GetTempPasswordIn() {
		super();
		setApiNumber(GET_TEMP_PASSWORD_API_NBR);
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
		return null;
	}

}
