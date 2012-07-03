/**
 *
 */
package org.irods.jargon.testutils.icommandinvoke.icommands;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.testutils.icommandinvoke.IcommandException;

/**
 * Implement the imkdir irods icommand
 * 
 * @author Mike Conway, DICE (www.irods.org)
 * @since 10/20/2009
 */
public class ImkdirCommand implements Icommand {

	private String collectionName = "";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.icommandinvoke.icommands.Icommand#buildCommand()
	 */

	@Override
	public List<String> buildCommand() throws IcommandException {
		if (collectionName == null || collectionName.length() == 0) {
			throw new IllegalArgumentException("no collection name specified");
		}
		List<String> commands = new ArrayList<String>();
		commands.add("imkdir");
		commands.add("-p"); // currently defaults to create parent dirs as
							// needed
		commands.add(collectionName);
		return commands;
	}

	/**
	 * Full path of the directory (collection) to create in irods
	 * 
	 * @return the collectionName
	 */
	public String getCollectionName() {
		return collectionName;
	}

	/**
	 * Set full path of the directory (collection) to create in irods
	 * 
	 * @param collectionName
	 *            the collectionName to set
	 */
	public void setCollectionName(final String collectionName) {
		this.collectionName = collectionName;
	}

}
