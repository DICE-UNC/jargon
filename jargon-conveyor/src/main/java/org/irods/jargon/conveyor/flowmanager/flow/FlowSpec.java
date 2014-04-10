/**
 * 
 */
package org.irods.jargon.conveyor.flowmanager.flow;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specification for a single flow. This is a workflow chain
 * associated with a selector
 * 
 * @author Mike Conway - DICE
 * 
 */
public class FlowSpec {

	private Selector selector = new Selector();
	private MicroserviceDef condition;
	private List<MicroserviceDef> preOperationChain = new ArrayList<MicroserviceDef>();
	private List<MicroserviceDef> preFileChain = new ArrayList<MicroserviceDef>();
	private List<MicroserviceDef> postFileChain = new ArrayList<MicroserviceDef>();
	private List<MicroserviceDef> postOperationChain = new ArrayList<MicroserviceDef>();
	public Selector getSelector() {
		return selector;
	}
	public void setSelector(Selector selector) {
		this.selector = selector;
	}
	public MicroserviceDef getCondition() {
		return condition;
	}
	public void setCondition(MicroserviceDef condition) {
		this.condition = condition;
	}
	public List<MicroserviceDef> getPreOperationChain() {
		return preOperationChain;
	}
	public void setPreOperationChain(List<MicroserviceDef> preOperationChain) {
		this.preOperationChain = preOperationChain;
	}
	public List<MicroserviceDef> getPreFileChain() {
		return preFileChain;
	}
	public void setPreFileChain(List<MicroserviceDef> preFileChain) {
		this.preFileChain = preFileChain;
	}
	public List<MicroserviceDef> getPostFileChain() {
		return postFileChain;
	}
	public void setPostFileChain(List<MicroserviceDef> postFileChain) {
		this.postFileChain = postFileChain;
	}
	public List<MicroserviceDef> getPostOperationChain() {
		return postOperationChain;
	}
	public void setPostOperationChain(List<MicroserviceDef> postOperationChain) {
		this.postOperationChain = postOperationChain;
	}

	
	

}
