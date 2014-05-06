import org.irods.jargon.conveyor.flowmanager.flow.*
import org.irods.jargon.conveyor.flowmanager.flow.Selector.FlowActionEnum
import org.irods.jargon.conveyor.flowmanager.flow.dsl.*
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.CancelOperationMicroservice
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.EnqueueTransferMicroservice
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.InspectForBundleOperationMicroservice
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.TarCollectionMicroservice

String bundleConditionMicroservice = InspectForBundleOperationMicroservice.class.getName()
String tarMicroservice = TarCollectionMicroservice.class.getName()
String enqueueMicroservice = EnqueueTransferMicroservice.class.getName()
String cancelMicroservice = CancelOperationMicroservice.class.getName()

FlowSpec flowSpec = Flow.define().forAction(FlowActionEnum.PUT)
		.forAnyHost()
		.forAnyZone()
		.when(bundleConditionMicroservice)
		.addPreOperationMicroservice(tarMicroservice)
		.addPreOperationMicroservice(enqueueMicroservice)
		.addPreOperationMicroservice(cancelMicroservice)
		.endPreOperationChain()
		.endPreFileChain()
		.endPostFileChain()
		.endPostOperationChain()
		.endFlowWithoutErrorHandler()

return flowSpec
