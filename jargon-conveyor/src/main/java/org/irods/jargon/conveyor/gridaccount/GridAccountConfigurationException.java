/**
 * 
 */
package org.irods.jargon.conveyor.gridaccount;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;

/**
 * Exception caused in grid account configuration process
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class GridAccountConfigurationException extends
		ConveyorExecutionException {
	
	private static final long serialVersionUID = 5002386897952197209L;

	public GridAccountConfigurationException() {
		super();
	}

	public GridAccountConfigurationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public GridAccountConfigurationException(String arg0) {
		super(arg0);
	}

	public GridAccountConfigurationException(Throwable arg0) {
		super(arg0);
	}

	

}
