import org.irods.jargon.conveyor.flowmanager.flow.*
import org.irods.jargon.conveyor.flowmanager.flow.dsl.*
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.LogAndContinueMicroservice
import org.irods.jargon.conveyor.flowmanager.microservice.builtins.SkipThisFileMicroservice

String logAndContinueFqcn = LogAndContinueMicroservice.class.getName();
String skipMeFqcn = SkipThisFileMicroservice.class.getName();

FlowSpec flow = Flow.define().forAnyAction()
		.forAnyHost()
		.forAnyZone()
		.onAllConditions()
		.endPreOperationChain()
		.addPreFileMicroservice(logAndContinueFqcn)
		.addPreFileMicroservice(skipMeFqcn)
		.endPreFileChain()
		.endPostFileChain().endPostOperationChain()
		.endFlowWithoutErrorHandler();

return flow