/**
 *
 */
package org.irods.jargon.testutils.icommandinvoke.icommands;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike
 * 
 */
public class IlsCommand implements Icommand {

	private String ilsBasePath = "";
	private boolean longFormat = false;

	public boolean isLongFormat() {
		return longFormat;
	}

	public void setLongFormat(final boolean longFormat) {
		this.longFormat = longFormat;
	}

	public String getIlsBasePath() {
		return ilsBasePath;
	}

	public void setIlsBasePath(final String ilsBasePath) {
		this.ilsBasePath = ilsBasePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.icommandinvoke.icommands.Icommand#buildCommand()
	 */
	@Override
	public List<String> buildCommand() {
		List<String> commandProps = new ArrayList<String>();
		commandProps.add("ils");
		if (longFormat) {
			commandProps.add("-l");
		}
		if (ilsBasePath.length() > 0) {
			commandProps.add(ilsBasePath);
		}
		return commandProps;
	}

}
