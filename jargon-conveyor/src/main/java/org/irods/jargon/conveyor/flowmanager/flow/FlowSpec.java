/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specification for a single flow. This is a workflow chain
 * associated with a selector
 * <p/>
 * Note that the various microservices are expressed as Strings that represent
 * the fully qualified class name, and these will be validated as the FlowSpecs
 * are created. The microservice instances are created when each new flow runs
 * from this information
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FlowSpec implements Cloneable {

	private Selector selector = new Selector();
	private String condition;
	private List<String> preOperationChain = new ArrayList<String>();
	private List<String> preFileChain = new ArrayList<String>();
	private List<String> postFileChain = new ArrayList<String>();
	private List<String> postOperationChain = new ArrayList<String>();
	private String errorHandler;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public synchronized FlowSpec clone() {
		FlowSpec clone = new FlowSpec();
		clone.setSelector(selector.clone());

		if (condition != null) {
			clone.setCondition(new String(condition));
		}

		ArrayList<String> clonePreOperationChain = new ArrayList<String>(
				preOperationChain.size());
		for (String preop : preOperationChain) {
			clonePreOperationChain.add(new String(preop));
		}

		ArrayList<String> clonePreFileChain = new ArrayList<String>(
				preFileChain.size());
		for (String preop : preFileChain) {
			clonePreFileChain.add(new String(preop));
		}

		ArrayList<String> clonePostFileChain = new ArrayList<String>(
				postFileChain.size());
		for (String postop : postFileChain) {
			clonePostFileChain.add(new String(postop));
		}

		ArrayList<String> clonePostOpChain = new ArrayList<String>(
				postOperationChain.size());
		for (String postop : postOperationChain) {
			clonePostOpChain.add(new String(postop));
		}

		clone.setPostFileChain(clonePostFileChain);
		clone.setPostOperationChain(clonePostOpChain);
		clone.setPreFileChain(clonePreFileChain);
		clone.setPreOperationChain(clonePreOperationChain);
		if (errorHandler != null) {
			clone.setErrorHandler(new String(errorHandler));
		}
		return clone;

	}

	public synchronized Selector getSelector() {
		return selector;
	}

	public synchronized void setSelector(final Selector selector) {
		this.selector = selector;
	}

	public synchronized String getCondition() {
		return condition;
	}

	public synchronized void setCondition(final String condition) {
		this.condition = condition;
	}

	public synchronized List<String> getPreOperationChain() {
		return preOperationChain;
	}

	public synchronized void setPreOperationChain(
			final List<String> preOperationChain) {
		this.preOperationChain = preOperationChain;
	}

	public synchronized List<String> getPreFileChain() {
		return preFileChain;
	}

	public synchronized void setPreFileChain(final List<String> preFileChain) {
		this.preFileChain = preFileChain;
	}

	public synchronized List<String> getPostFileChain() {
		return postFileChain;
	}

	public synchronized void setPostFileChain(final List<String> postFileChain) {
		this.postFileChain = postFileChain;
	}

	public synchronized List<String> getPostOperationChain() {
		return postOperationChain;
	}

	public synchronized void setPostOperationChain(
			final List<String> postOperationChain) {
		this.postOperationChain = postOperationChain;
	}

	public synchronized String getErrorHandler() {
		return errorHandler;
	}

	public synchronized void setErrorHandler(final String errorHandler) {
		this.errorHandler = errorHandler;
	}

}
