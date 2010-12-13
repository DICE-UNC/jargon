/**
 * 
 */
package edu.sdsc.jargon.testutils.icommandinvoke.icommands;

import java.util.ArrayList;
import java.util.List;

import edu.sdsc.jargon.testutils.icommandinvoke.IcommandException;

/**
 * Remove and IRODS user
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class RemoveUserCommand extends ImetaCommand {

	private String userName = "";

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
		
		if (userName == null || userName.length() == 0) {
			throw new IcommandException("did not provide a user name");
		}
		
		List<String> command = new ArrayList<String>();

		command.add("iadmin");
		command.add("rmuser");
		command.add(userName);
		return command;
	}

}
