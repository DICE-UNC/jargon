import org.irods.jargon.conveyor.flowmanager.flow.*
import org.irods.jargon.conveyor.flowmanager.flow.dsl.*

FlowSpec flowSpec = Flow.define()
		.forAnyAction()
		.forAnyHost()
		.forAnyZone()
		.onAllConditions()
		.addPreOperationMicroservice("org.irods.jargon.conveyor.flowmanager.microservice.builtins.LogAndContinueMicroservice").endPreOperationChain()
		.endPreFileChain()
		.endPostFileChain()
		.endPostOperationChain()
		.endFlowWithoutErrorHandler()

return flowSpec