package org.irods.jargon.core.query;

import org.junit.Test;

import junit.framework.Assert;

public class AVUQueryElementTest {

	@Test
	public final void testInstanceForValueQuery() throws JargonQueryException {
		Assert.assertNotNull(AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE,
				AVUQueryOperatorEnum.EQUAL, "3"));
	}

	@Test(expected = JargonQueryException.class)
	public final void testInstanceForValueQueryNullPart() throws JargonQueryException {
		AVUQueryElement.instanceForValueQuery(null, AVUQueryOperatorEnum.EQUAL, "3");
	}

	@Test(expected = JargonQueryException.class)
	public final void testInstanceForValueQueryNullOperation() throws JargonQueryException {
		AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE, null, "3");
	}

	@Test(expected = JargonQueryException.class)
	public final void testInstanceForValueQueryNullValue() throws JargonQueryException {
		AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL, null);
	}

	@Test(expected = JargonQueryException.class)
	public final void testInstanceForValueQueryEmptyValue() throws JargonQueryException {
		AVUQueryElement.instanceForValueQuery(AVUQueryElement.AVUQueryPart.ATTRIBUTE, AVUQueryOperatorEnum.EQUAL, "");
	}
}
