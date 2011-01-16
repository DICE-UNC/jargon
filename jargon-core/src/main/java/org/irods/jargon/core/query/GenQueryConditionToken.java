/**
 * 
 */
package org.irods.jargon.core.query;

/**
 * Inernally used to classify parts of a query condition when translating from
 * String form to a representation. This is not immutable, but is not used
 * outside of query translation.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
class GenQueryConditionToken {

	public enum TokenType {
		FIELD, OPERATOR, LITERAL, CONNECTOR
	}

	private String value;
	private TokenType tokenType;

	public String getValue() {
		return value;
	}

	public TokenType getTokenType() {
		return tokenType;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public void setTokenType(final TokenType tokenType) {
		this.tokenType = tokenType;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("QueryConditionToken:");
		sb.append("\n   value:");
		sb.append(value);
		sb.append("\n   type:");
		sb.append(tokenType);
		return sb.toString();
	}

}
