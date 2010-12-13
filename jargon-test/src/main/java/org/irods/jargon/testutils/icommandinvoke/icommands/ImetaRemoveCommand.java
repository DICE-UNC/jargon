/**
 * 
 */
package org.irods.jargon.testutils.icommandinvoke.icommands;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.testutils.icommandinvoke.IcommandException;


/**
 * Remove a given piece of metadata
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ImetaRemoveCommand extends ImetaCommand {
	
	private String attribName = "";
	private String attribValue = "";
	private String attribUnits = "";

	public String getAttribValue() {
		return attribValue;
	}

	public void setAttribValue(String attribValue) {
		this.attribValue = attribValue;
	}

	public String getAttribUnits() {
		return attribUnits;
	}

	public void setAttribUnits(String attribUnits) {
		this.attribUnits = attribUnits;
	}

	public String getAttribName() {
		return attribName;
	}

	public void setAttribName(String attribName) {
		this.attribName = attribName;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.testutils.icommandinvoke.icommands.Icommand#buildCommand
	 * ()
	 */
	public List<String> buildCommand() throws IcommandException {
		if (this.getObjectPath() == null || this.getObjectPath().length() == 0) {
			throw new IllegalArgumentException("must supply an object path");
		}

		if (attribName == null || attribValue == null) {
			throw new IllegalArgumentException(
					"attrib name, value, and units must not be null, leave as spaces if not used");
		}
		
		if (attribName.length() == 0 || attribValue.length() == 0) {
			throw new IllegalArgumentException("attrib name and value are required");

		}

		List<String> command = new ArrayList<String>();

		command.add("imeta");
		command.add("rm");
		command.add(this.translateMetaObjectTypeToString(getMetaObjectType()));
		command.add(this.getObjectPath());
		command.add(this.getAttribName());
		command.add(this.getAttribValue());
		if (this.getAttribUnits().length() > 0) {
			command.add(this.getAttribUnits());
		}
		return command;
	}

}
