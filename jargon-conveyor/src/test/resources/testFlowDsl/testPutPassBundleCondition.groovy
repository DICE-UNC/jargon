import org.irods.jargon.conveyor.flowmanager.flow.*
import org.irods.jargon.conveyor.flowmanager.flow.dsl.*
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.InspectForBundleOperationMicroservice

String fqcn = InspectForBundleOperationMicroservice.class.getName()

FlowSpec flow =Flow.define().forAction(FlowActionEnum.PUT).forAnyHost().forAnyZone()
		.when(fqcn)
		.endPreOperationChain()
		.endPreFileChain()
		.endPostFileChain().endPostOperationChain()
		.endFlowWithoutErrorHandler();

return flow