/**
 *
 */
package org.irods.jargon.core.pub.domain;

/**
 * Representation of an AVU metadata item. This class is mutable and should be
 * used carefully. Jargon libraries will not alter the values of mutable objects
 * used as input or output parameters to methods unless specifically documented.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */

public final class AvuData extends IRODSDomainObject {

	private String attribute = "";
	private String value = "";
	private String unit = "";

	/**
	 * Static initializer returns an {@code AvuData}. Note that unused values should
	 * be set to 'blank' rather then {@code null}. An
	 * {@code IllegalArgumentException} will be thrown if something is null.
	 *
	 * @param attribute
	 *            {@code String} with the AVU attribute.
	 * @param value
	 *            {@code String} with the AVU value.
	 * @param unit
	 *            {@code String} with AVU unit.
	 * @return {@link AvuData}
	 *
	 */
	public static AvuData instance(final String attribute, final String value, final String unit) {
		return new AvuData(attribute, value, unit);
	}

	public AvuData() {

	}

	/**
	 * Constructor for AVU that takes the attribute, value, unit
	 *
	 * @param attribute
	 *            {@code String} with the AVU attribute.
	 * @param value
	 *            {@code String} with the AVU value.
	 * @param unit
	 *            {@code String} with AVU unit.
	 *
	 */
	public AvuData(final String attribute, final String value, final String unit) {
		if (attribute == null || attribute.isEmpty()) {
			throw new IllegalArgumentException("attribute is null or empty");
		}

		if (value == null || value.isEmpty()) {
			throw new IllegalArgumentException("value is null or empty");
		}

		if (unit == null) {
			throw new IllegalArgumentException("unit is null, leave blank String if empty");
		}

		this.attribute = attribute;
		this.value = value;
		this.unit = unit;

	}

	public String getAttribute() {
		return attribute;
	}

	public String getValue() {
		return value;
	}

	public String getUnit() {
		return unit;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("avu data:\n");
		sb.append("   attrib:");
		sb.append(attribute);
		sb.append("\n   value:");
		sb.append(value);
		sb.append("\n   units:");
		sb.append(unit);
		return sb.toString();
	}

	public void setAttribute(final String attribute) {
		this.attribute = attribute;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public void setUnit(final String unit) {
		this.unit = unit;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof AvuData)) {
			return false;
		}

		AvuData other = (AvuData) obj;
		return (attribute.equals(other.attribute) && value.equals(other.value) && unit.equals(other.unit));
	}

	@Override
	public int hashCode() {
		return attribute.hashCode() + value.hashCode() + unit.hashCode();
	}

}
