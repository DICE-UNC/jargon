/**
 * 
 */
package org.irods.jargon.testutils.icommandinvoke.icommands;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.testutils.icommandinvoke.IcommandException;

/**
 * List AVU metadata
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ImetaListCommand extends ImetaCommand {

	private String attribName = "";

	public String getAttribName() {
		return attribName;
	}

	public void setAttribName(final String attribName) {
		this.attribName = attribName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.sdsc.jargon.testutils.icommandinvoke.icommands.Icommand#buildCommand
	 * ()
	 */
	@Override
	public List<String> buildCommand() throws IcommandException {
		if (getObjectPath() == null || getObjectPath().length() == 0) {
			throw new IllegalArgumentException("must supply an object path");
		}

		if (attribName == null) {
			throw new IllegalArgumentException(
					"attrib name must not be null, leave as spaces if not used");
		}

		List<String> command = new ArrayList<String>();

		command.add("imeta");
		command.add("ls");
		command.add(translateMetaObjectTypeToString(getMetaObjectType()));
		command.add(getObjectPath());
		if (attribName.length() > 0) {
			command.add(attribName);
		}

		return command;

	}

}
