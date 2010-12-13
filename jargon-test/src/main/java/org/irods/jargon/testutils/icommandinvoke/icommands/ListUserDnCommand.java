/**
 * 
 */
package org.irods.jargon.testutils.icommandinvoke.icommands;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.testutils.icommandinvoke.IcommandException;


/**
 * Get a user's DN
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ListUserDnCommand extends ImetaCommand {

	private String userDn = "";
	
	public String getUserDn() {
		return userDn;
	}

	public void setUserDn(String userDn) {
		this.userDn = userDn;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.testutils.icommandinvoke.icommands.Icommand#buildCommand
	 * ()
	 */
	public List<String> buildCommand() throws IcommandException {
		
		if (userDn == null || userDn.length() == 0) {
			throw new IcommandException("user dn must be supplied");
		}
		
		List<String> command = new ArrayList<String>();

		command.add("iadmin");
		command.add("luan");
		command.add(userDn);
		return command;
	}

}
