/**
 * 
 */
package edu.sdsc.jargon.testutils.icommandinvoke.icommands;

import java.util.ArrayList;
import java.util.List;

import edu.sdsc.jargon.testutils.icommandinvoke.IcommandException;

/**
 * List users in irods
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ListUsersCommand extends ImetaCommand {

	String userName = "";

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.sdsc.jargon.testutils.icommandinvoke.icommands.Icommand#buildCommand
	 * ()
	 */
	public List<String> buildCommand() throws IcommandException {
		if (userName == null) {
			throw new IcommandException(
					"user name cannot be null, set to spaces");
		}

		List<String> command = new ArrayList<String>();

		command.add("iadmin");
		command.add("lu");
		if (userName.length() > 0) {

			command.add(userName);
		}
		
		return command;
	}

}
