/**
 * 
 */
package org.irods.jargon.core.query;

import java.util.List;

/**
 * Describes an element of an AVU query (e.g. attribute = some value, units like
 * some value). These are then used in groups to define a particular metadata
 * query.
 * <p/>
 * Note that this is used by the older 'string' query technique, which uses
 * iquest like queries, and is not used in the recommended
 * <code>IRODSGenQueryBuilder</code> query technique.
 * <p/>
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

	private final AVUQueryPart avuQueryPart;
	private final AVUQueryOperatorEnum operator;
	private final String value;
	private final String valueEndOfRange;
	private final List<Object> valuesTable;

	public static AVUQueryElement instanceForValueQuery(
			final AVUQueryPart avuQueryPart,
			final AVUQueryOperatorEnum operator, final String value)
			throws JargonQueryException {
		return new AVUQueryElement(avuQueryPart, operator, value, null, null);
	}

	private AVUQueryElement(final AVUQueryPart avuQueryPart,
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
		this.value = value.trim();
		this.valueEndOfRange = valueEndOfRange;
		this.valuesTable = null;

		// TODO: implement additional avu query values below

		if (valueEndOfRange != null) {
			throw new JargonQueryException("currently unsupported");
		}

		if (valuesTable != null) {
			throw new JargonQueryException("currently unsupported");
		}

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
