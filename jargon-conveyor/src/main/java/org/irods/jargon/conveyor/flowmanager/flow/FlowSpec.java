/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.conveyor.flowmanager.microservice.ConditionMicroservice;
import org.irods.jargon.conveyor.flowmanager.microservice.ErrorHandlerMicroservice;
import org.irods.jargon.conveyor.flowmanager.microservice.Microservice;

/**
 * Represents a specification for a single flow. This is a workflow chain
 * associated with a selector
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FlowSpec {

	private Selector selector = new Selector();
	private ConditionMicroservice condition;
	private List<Microservice> preOperationChain = new ArrayList<Microservice>();
	private List<Microservice> preFileChain = new ArrayList<Microservice>();
	private List<Microservice> postFileChain = new ArrayList<Microservice>();
	private List<Microservice> postOperationChain = new ArrayList<Microservice>();
	private ErrorHandlerMicroservice errorHandler;

	public Selector getSelector() {
		return selector;
	}

	public void setSelector(Selector selector) {
		this.selector = selector;
	}

	public ConditionMicroservice getCondition() {
		return condition;
	}

	public void setCondition(ConditionMicroservice condition) {
		this.condition = condition;
	}

	public List<Microservice> getPreOperationChain() {
		return preOperationChain;
	}

	public void setPreOperationChain(List<Microservice> preOperationChain) {
		this.preOperationChain = preOperationChain;
	}

	public List<Microservice> getPreFileChain() {
		return preFileChain;
	}

	public void setPreFileChain(List<Microservice> preFileChain) {
		this.preFileChain = preFileChain;
	}

	public List<Microservice> getPostFileChain() {
		return postFileChain;
	}

	public void setPostFileChain(List<Microservice> postFileChain) {
		this.postFileChain = postFileChain;
	}

	public List<Microservice> getPostOperationChain() {
		return postOperationChain;
	}

	public void setPostOperationChain(List<Microservice> postOperationChain) {
		this.postOperationChain = postOperationChain;
	}

	public ErrorHandlerMicroservice getErrorHandler() {
		return errorHandler;
	}

	public void setErrorHandler(ErrorHandlerMicroservice errorHandler) {
		this.errorHandler = errorHandler;
	}

}
