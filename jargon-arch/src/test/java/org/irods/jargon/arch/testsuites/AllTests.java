package org.irods.jargon.arch.testsuites;

import org.irods.jargon.arch.mvc.controllers.PolicyDrivenServiceManagementControllerTest;
import org.irods.jargon.arch.mvc.controllers.PolicyManagerControllerTest;
import org.irods.jargon.arch.mvc.controllers.RuleDirAdminControllerTest;
import org.irods.jargon.arch.mvc.controllers.RuleMappingControllerTest;
import org.irods.jargon.arch.mvc.controllers.SeriesControllerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses( { 
	PolicyDrivenServiceManagementControllerTest.class, RuleDirAdminControllerTest.class, RuleMappingControllerTest.class, PolicyManagerControllerTest.class,
	SeriesControllerTest.class
})
public class AllTests {

}
