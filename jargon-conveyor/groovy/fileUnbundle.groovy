import org.irods.jargon.conveyor.flowmanager.flow.*
import org.irods.jargon.conveyor.flowmanager.flow.Selector.FlowActionEnum
import org.irods.jargon.conveyor.flowmanager.flow.dsl.*
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.ExtractBundleMicroservice
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.InspectForUnbundleOperationMicroservice

String bundleConditionMicroservice = InspectForUnbundleOperationMicroservice.class.getName()
String unbundle = ExtractBundleMicroservice.class.getName()

FlowSpec flowSpec = Flow.define().forAction(FlowActionEnum.PUT)
		.forAnyHost()
		.forAnyZone()
		.when(bundleConditionMicroservice)
		.endPreOperationChain()
		.endPreFileChain()
		.addPostFileMicroservice(unbundle)
		.endPostFileChain()
		.endPostOperationChain()
		.endFlowWithoutErrorHandler()

return flowSpec
