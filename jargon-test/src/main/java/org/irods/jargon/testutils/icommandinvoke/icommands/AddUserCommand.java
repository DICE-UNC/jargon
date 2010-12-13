/**
 * 
 */
package org.irods.jargon.testutils.icommandinvoke.icommands;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.testutils.icommandinvoke.IcommandException;


/**
 * Add an irods user
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class AddUserCommand extends ImetaCommand {

	private String userName = "";
	private String userType = "rodsuser";

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

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
	 * org.irods.jargon.testutils.icommandinvoke.icommands.Icommand#buildCommand
	 * ()
	 */
	public List<String> buildCommand() throws IcommandException {
		
		if (userName == null || userName.length() == 0) {
			throw new IcommandException("did not provide a user name");
		}
		
		if (userType == null || userType.length() == 0) {
			throw new IcommandException("did not provide a user type");
		}
		
		List<String> command = new ArrayList<String>();

		command.add("iadmin");
		command.add("mkuser");
		command.add(userName);
		command.add(userType);
		return command;
	}

}
