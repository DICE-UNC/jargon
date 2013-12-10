/**
 * 
 */
package org.irods.jargon.conveyor.gridaccount;

import org.irods.jargon.conveyor.core.ConveyorExecutionException;

/**
 * Exception caused in grid account configuration process
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class GridAccountConfigurationException extends
		ConveyorExecutionException {

	private static final long serialVersionUID = 5002386897952197209L;

	public GridAccountConfigurationException() {
		super();
	}

	public GridAccountConfigurationException(final String arg0,
			final Throwable arg1) {
		super(arg0, arg1);
	}

	public GridAccountConfigurationException(final String arg0) {
		super(arg0);
	}

	public GridAccountConfigurationException(final Throwable arg0) {
		super(arg0);
	}

}
