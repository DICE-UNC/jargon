/**
 * 
 */
package org.irods.jargon.core.packinstr;

/**
 * Structure that is the result of a client-server negotiation, analgous to the
 * cs_neg_t struct in iRODS
 * 
 * @author Mike Conway - DICE
 *
 */
public class ClientServerNegotiationStruct {

	private int status = 0;
	private String result = "";

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClientServerNegotiationStruct [status=").append(status)
				.append(", ");
		if (result != null) {
			builder.append("result=").append(result);
		}
		builder.append("]");
		return builder.toString();
	}

}
