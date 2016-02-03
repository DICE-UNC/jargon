/**
 * 
 */
package org.irods.jargon.mdquery;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.irods.jargon.core.query.AVUQueryOperatorEnum;

/**
 * Simple POJO for an element of a query, suitable for serialization
 * 
 * @author Mike Conway - DICE
 *
 */
@XmlRootElement
public class MetadataQueryElement {

	/**
	 * AVU name for the query
	 */
	private String attributeName = "";
	/**
	 * Operator for the query
	 */
	private AVUQueryOperatorEnum operator = AVUQueryOperatorEnum.EQUAL;
	/**
	 * Value for the query, which may be an array of one for a normal query, or
	 * two for between, or many for an 'in' query
	 */
	private List<String> attributeValue = new ArrayList<String>();
	/**
	 * Connector to tie this query with the following entry, typically an AND,
	 * but will support one OR
	 */
	private Connector connector = Connector.AND;

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("MetadataQueryElement [");
		if (attributeName != null) {
			builder.append("attributeName=").append(attributeName).append(", ");
		}
		if (operator != null) {
			builder.append("operator=").append(operator).append(", ");
		}
		if (attributeValue != null) {
			builder.append("value=")
					.append(attributeValue.subList(0, Math.min(attributeValue.size(), maxLen)))
					.append(", ");
		}
		if (connector != null) {
			builder.append("connector=").append(connector);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Connector enum (and/or) to the next element
	 *
	 */
	public enum Connector {
		AND, OR
	}

	/**
	 * 
	 */
	public MetadataQueryElement() {

	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public AVUQueryOperatorEnum getOperator() {
		return operator;
	}

	public void setOperator(AVUQueryOperatorEnum operator) {
		this.operator = operator;
	}

	public List<String> getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(List<String> value) {
		this.attributeValue = value;
	}

	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

}
