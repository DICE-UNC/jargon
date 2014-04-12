/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.FlowManagerService;

/**
 * Flow Manager service handles client-side actions and workflows
 * 
 * @author Mike Conway - DICE
 *
 */
public class BasicFlowManagerService extends AbstractConveyorComponentService
		implements FlowManagerService {

	@SuppressWarnings("unused")
	private final List<String> flowDirectories;

	/**
	 * 
	 */
	public BasicFlowManagerService(final List<String> flowDirectories) {

		// make a copy of the flow dirs array and make sure it's immutable

		List<String> myFlowDirectories = new ArrayList<String>();

		for (String orig : flowDirectories) {
			myFlowDirectories.add(orig);
		}

		this.flowDirectories = Collections.unmodifiableList(myFlowDirectories);

	}

}
