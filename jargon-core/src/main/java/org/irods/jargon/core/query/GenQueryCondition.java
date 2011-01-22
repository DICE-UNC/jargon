/**
 * 
 */
package org.irods.jargon.core.query;

/**
 * Represents the field name, operator, and value for a condition in an IRODS
 * query. This is an internal representation for the query parser. This is an
 * immutable object, and is thread-safe.
 * 
 * @author mikeconway
 * 
 */
public class GenQueryCondition {
	private final String fieldName;
	private final String operator;
	private final String value;

	public static GenQueryCondition instance(final String fieldName,
			final String operator, final String value)
			throws JargonQueryException {
		return new GenQueryCondition(fieldName, operator, value);
	}

	private GenQueryCondition(final String fieldName, final String operator,
			final String value) throws JargonQueryException {
		if (fieldName == null) {
			throw new JargonQueryException("field name in condition is null");
		}

		if (operator == null) {
			throw new JargonQueryException("operator is null");
		}

		if (value == null) {
			throw new JargonQueryException("value in condition is null");
		}

		this.fieldName = fieldName;
		this.operator = operator;
		this.value = value;

	}

	public String getFieldName() {
		return fieldName;
	}

	public String getOperator() {
		return operator;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		String tabOver = "    ";
		char cr = '\n';
		b.append("query element:");
		b.append(cr);
		b.append(tabOver);
		b.append("fieldName:");
		b.append(fieldName);
		b.append(cr);
		b.append(tabOver);
		b.append("Operator:");
		b.append(operator);
		b.append(cr);
		b.append(tabOver);
		b.append("Value:");
		b.append(value);
		return b.toString();
	}

}
