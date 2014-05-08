/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow.dsl;

import junit.framework.Assert;

import org.irods.jargon.conveyor.flowmanager.flow.FlowSpec;
import org.irods.jargon.conveyor.flowmanager.flow.Selector.FlowActionEnum;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;
import org.junit.Test;

/**
 * Test for building Flow via DSL
 * 
 * @author Mike Conway - DICE
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

	@Test
	public void testDefineForAnyAction() {
		FlowHostSelectorSpecification actual = Flow.define().forAnyAction();
		Assert.assertEquals("did not set flow action", FlowActionEnum.ANY,
				actual.getFlowSpec().getSelector().getFlowActionEnum());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDefineNullAction() {
		Flow.define().forAction(null);

	}

	@Test
	public void testSelectHostBlahStar() throws Exception {
		FlowZoneSelectorSpecification actual = Flow.define().forAnyAction()
				.forHost("blah*");
		Assert.assertEquals("did not set host to blah*", "blah*", actual
				.getFlowSpec().getSelector().getHostSelector());
	}

	@Test
	public void testSelectHostAny() throws Exception {
		FlowZoneSelectorSpecification actual = Flow.define().forAnyAction()
				.forAnyHost();
		Assert.assertEquals("did not set host to *", "*", actual.getFlowSpec()
				.getSelector().getHostSelector());
	}

	@Test
	public void testSelectZone() throws Exception {
		ConditionSpecification actual = Flow.define().forAnyAction()
				.forHost("blah*").forZone("zone");
		Assert.assertEquals("did not set zone to zone", "zone", actual
				.getFlowSpec().getSelector().getZoneSelector());
	}

	@Test
	public void testSelectAny() throws Exception {
		ConditionSpecification actual = Flow.define().forAnyAction()
				.forHost("blah*").forAnyZone();
		Assert.assertEquals("did not set zone to any", "*", actual
				.getFlowSpec().getSelector().getZoneSelector());
	}

	@Test
	public void testAddCondition() throws Exception {

		String fqcn = FlowTestConditionMicroservice.class.getName();

		PreOperationChainSpecification actual = Flow.define().forAnyAction()
				.forHost("blah*").forZone("zone").when(fqcn);
		Assert.assertNotNull("did not set condition", actual.getFlowSpec()
				.getCondition());
	}

	@Test(expected = FlowSpecificationException.class)
	public void testAddBogusCondition() throws Exception {

		String fqcn = "arglebargle";

		Flow.define().forAnyAction().forHost("blah*").forZone("zone")
				.when(fqcn);

	}

	@Test(expected = FlowSpecificationException.class)
	public void testAddConditionNotConditionMS() throws Exception {

		Flow.define().forAnyAction().forHost("blah*").forZone("zone")
				.when(Microservice.class.getName());

	}

	@Test
	public void testAddFlowOnePreOpMicroservice() throws Exception {

		String fqcn = Microservice.class.getName();

		PreOperationChainSpecification actual = Flow.define().forAnyAction()
				.forHost("blah*").forZone("zone").onAllConditions()
				.addPreOperationMicroservice(fqcn);

		Assert.assertEquals("did not set one ms in pre op flow", 1, actual
				.getFlowSpec().getPreOperationChain().size());

	}

	@Test
	public void testAddFlowOnePreFileMicroservice() throws Exception {

		String fqcn = Microservice.class.getName();

		PreFileChainSpecification actual = Flow.define().forAnyAction()
				.forHost("blah*").forZone("zone").onAllConditions()
				.endPreOperationChain().addPreFileMicroservice(fqcn);

		Assert.assertEquals("did not set one ms in pre file flow", 1, actual
				.getFlowSpec().getPreFileChain().size());

	}

	@Test
	public void testAddFlowOnePostFileMicroservice() throws Exception {

		String fqcn = Microservice.class.getName();

		PostFileChainSpecification actual = Flow.define().forAnyAction()
				.forHost("blah*").forZone("zone").onAllConditions()
				.endPreOperationChain().endPreFileChain()
				.addPostFileMicroservice(fqcn);

		Assert.assertEquals("did not set one ms in post file flow", 1, actual
				.getFlowSpec().getPostFileChain().size());

	}

	@Test
	public void testAddFlowOnePostFileMicroserviceComplete() throws Exception {

		String fqcn = Microservice.class.getName();

		FlowSpec actual = Flow.define().forAnyAction().forHost("blah*")
				.forZone("zone").onAllConditions().endPreOperationChain()
				.endPreFileChain().addPostFileMicroservice(fqcn)
				.endPostFileChain().endPostOperationChain()
				.endFlowWithoutErrorHandler();

		Assert.assertNotNull("null flowspec", actual);

	}

}
