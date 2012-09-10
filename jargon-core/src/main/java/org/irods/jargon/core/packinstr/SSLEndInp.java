package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;

/**
 * Signals a request to iRODS to end SSL on the connection (this is currently
 * limited to exchange of credentials)
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class SSLEndInp extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "sslEndtInp_PI";
	public static final int SSL_END_API_NBR = 1101;

	/**
	 * Static initializer creates SSL start request
	 * 
	 * @return <code>SSLStartInp</code> packing instruction
	 */
	public static final SSLEndInp instance() {
		return new SSLEndInp();
	}

	/**
	 * Default (no-values) constructor
	 */
	private SSLEndInp() {
		this.setApiNumber(SSL_END_API_NBR);
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction#getTagValue()
	 */
	@Override
	public Tag getTagValue() throws JargonException {
		Tag message = new Tag(PI_TAG);
		message.addTag("arg0", "");
		return message;
	}

}
