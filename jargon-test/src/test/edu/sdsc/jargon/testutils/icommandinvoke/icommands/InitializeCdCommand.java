/**
 *
 */
package edu.sdsc.jargon.testutils.icommandinvoke.icommands;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Conway, DICE (www.irods.org)
 *
 */
public class InitializeCdCommand implements Icommand {

	private String targetCollection = "";

	public String getTargetCollection() {
		return targetCollection;
	}

	public void setTargetCollection(String targetCollection) {
		this.targetCollection = targetCollection;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.icommandinvoke.icommands.Icommand#buildCommand()
	 */
	
	public List<String> buildCommand() {
		List<String> commandProps = new ArrayList<String>();
		commandProps.add("icd");
		if (targetCollection.length() > 0) {
			commandProps.add(targetCollection);
		}
		return commandProps;
	}

	

}
