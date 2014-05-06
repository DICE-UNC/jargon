import org.irods.jargon.conveyor.flowmanager.flow.*
import org.irods.jargon.conveyor.flowmanager.flow.dsl.*

FlowSpec flow = Flow.define().forAnyAction()
		.forAnyHost()
		.forAnyZone()
		.onAllConditions()
		.endPreOperationChain()
		.endPreFileChain()
		.addPostFileMicroservice("org.irods.jargon.conveyor.flowmanager.microservice.builtins.PostFileAddTestAVUMicroservice")
		.endPostFileChain().endPostOperationChain()
		.endFlowWithoutErrorHandler();

return flow