/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import junit.framework.Assert;

import org.irods.jargon.conveyor.flowmanager.flow.Selector.FlowActionEnum;
import org.junit.Test;

/**
 * Test for building Flow via DSL
 * 
 * @author mikeconway
 *
 */
public class FlowTest {

	@Test
	public void testDefine() {
		FlowHostSelectorSpecification actual = Flow.define().forAction(
				FlowActionEnum.ANY);
		Assert.assertEquals("did not set flow action", FlowActionEnum.ANY,
				actual.getFlowSpec().getSelector().getFlowActionEnum());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDefineNullAction() {
		Flow.define().forAction(null);

	}

}
