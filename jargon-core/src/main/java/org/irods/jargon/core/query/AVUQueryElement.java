/**
 *
 */
package org.irods.jargon.core.query;

import java.util.List;

/**
 * Describes an element of an AVU query (e.g. attribute = some value, units like
 * some value). These are then used in groups to define a particular metadata
 * query.
 * <p>
 * Note that this is used by the older 'string' query technique, which uses
 * iquest like queries, and is not used in the recommended
 * {@code IRODSGenQueryBuilder} query technique.
 * <p>
 * This is a partial implementation of the code, and currently is limited in
 * usage.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class AVUQueryElement {
	public enum AVUQueryPart {
		ATTRIBUTE, VALUE, UNITS
	}

	private AVUQueryPart avuQueryPart;
	private AVUQueryOperatorEnum operator;
	private String value;
	private String valueEndOfRange;
	private List<Object> valuesTable;

	/**
	 * Create an instance of an {@code AVUQueryElement} that represents a
	 * component of a larger AVU query, specifiying the part (attrib, value, or
	 * unit), the operator, and the value to test agains
	 *
	 * @param avuQueryPart
	 *            {@link AVUQueryPart} discriminating between an attribute,
	 *            value, or unit
	 * @param operator
	 *            {@link AVUQueryOperatorEnum} that represents the operator in
	 *            the query condition
	 * @param value
	 *            {@code String} representing the actual value to test
	 *            against the operator for the given part of the query.
	 * @return {@link AVUQueryElement}
	 * @throws JargonQueryException
	 */
	public static AVUQueryElement instanceForValueQuery(
			final AVUQueryPart avuQueryPart,
			final AVUQueryOperatorEnum operator, final String value)
			throws JargonQueryException {
		return new AVUQueryElement(avuQueryPart, operator, value, null, null);
	}

	public AVUQueryElement(final AVUQueryPart avuQueryPart,
			final AVUQueryOperatorEnum operator, final String value,
			final String valueEndOfRange, final List<Object> valuesTable)
			throws JargonQueryException {

		if (avuQueryPart == null) {
			throw new JargonQueryException("avuQueryPart is null");
		}

		if (operator == null) {
			throw new JargonQueryException("avuQueryOperator is null");
		}

		if (value == null) {
			throw new JargonQueryException("null value");
		}

		this.avuQueryPart = avuQueryPart;
		this.operator = operator;
		this.value = value;
		this.valueEndOfRange = valueEndOfRange;
		this.valuesTable = null;

		if (valueEndOfRange != null) {
			throw new JargonQueryException("currently unsupported");
		}

		if (valuesTable != null) {
			throw new JargonQueryException("currently unsupported");
		}

	}

	/**
	 * Default (no values) constructor
	 */
	public AVUQueryElement() {

	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("AVUQueryElement [");
		if (avuQueryPart != null) {
			builder.append("avuQueryPart=").append(avuQueryPart).append(", ");
		}
		if (operator != null) {
			builder.append("operator=").append(operator).append(", ");
		}
		if (value != null) {
			builder.append("value=").append(value).append(", ");
		}
		if (valueEndOfRange != null) {
			builder.append("valueEndOfRange=").append(valueEndOfRange)
					.append(", ");
		}
		if (valuesTable != null) {
			builder.append("valuesTable=")
					.append(valuesTable.subList(0,
							Math.min(valuesTable.size(), maxLen)));
		}
		builder.append("]");
		return builder.toString();
	}

	public AVUQueryPart getAvuQueryPart() {
		return avuQueryPart;
	}

	public AVUQueryOperatorEnum getOperator() {
		return operator;
	}

	public String getValue() {
		return value;
	}

	public String getValueEndOfRange() {
		return valueEndOfRange;
	}

	public List<Object> getValuesTable() {
		return valuesTable;
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof AVUQueryElement)) {
			return false;
		}

		AVUQueryElement otherObj = (AVUQueryElement) other;

		return (avuQueryPart.equals(otherObj.avuQueryPart)
				&& operator.equals(otherObj.operator) && value
					.equals(otherObj.value));

	}

	@Override
	public int hashCode() {
		return avuQueryPart.hashCode() + operator.hashCode() + value.hashCode();
	}
}
