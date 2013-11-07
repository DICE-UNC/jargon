package org.irods.jargon.ruleservice.formatting;

import junit.framework.Assert;

import org.junit.Test;

public class HtmlLogTableFormatterTest {

	@Test
	public void testTabelizeBootstrap2() {
		String logData = "this\nis\na\nlog";
		String actual = HtmlLogTableFormatter.formatAsBootstrap2Table(logData,
				"");
		Assert.assertNotNull("did not build log table", actual);
		Assert.assertEquals(
				"<table class=\"table table-striped\"><caption></caption><tbody><tr><td>this</td></tr><tr><td>is</td></tr><tr><td>a</td></tr><tr><td>log</td></tr></tbody></table>",
				actual);
	}
}
