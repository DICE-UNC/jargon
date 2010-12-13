/**
 *
 */
package org.irods.jargon.testutils.icommandinvoke.icommands;

import static org.irods.jargon.testutils.TestingPropertiesHelper.*;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.testutils.icommandinvoke.IcommandException;
import static org.irods.jargon.testutils.TestingPropertiesHelper.*;

/**
 * Build an irepl command using the given properties for invocation by an
 * IcommandInvoker
 * 
 * @author Mike Conway, DICE (www.irods.org)
 * @since 10/16/2009
 * 
 */
public class IreplCommand implements Icommand {

	private String objectToReplicate = "";

	public String getObjectToReplicate() {
		return objectToReplicate;
	}

	public void setObjectToReplicate(String objectToReplicate) {
		this.objectToReplicate = objectToReplicate;
	}

	public String getDestResource() {
		return destResource;
	}

	public void setDestResource(String destResource) {
		this.destResource = destResource;
	}

	private String destResource = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.icommandinvoke.icommands.Icommand#buildCommand()
	 */
	public List<String> buildCommand() throws IcommandException {
		if (objectToReplicate == null || objectToReplicate.length() <= 0) {
			throw new IcommandException(
					"no IRODS object name provided for the irepl command");
		}

		if (destResource == null || destResource.length() <= 0) {

			throw new IcommandException(
					"no destination resource provided for the irepl command");
		}

		List<String> ireplCommand = new ArrayList<String>();
		ireplCommand.add("irepl");

		StringBuilder resourceProperty = new StringBuilder();
		resourceProperty.append("-R");
		resourceProperty.append(destResource);
		ireplCommand.add(resourceProperty.toString());

		ireplCommand.add(objectToReplicate);
		return ireplCommand;
	}

}
