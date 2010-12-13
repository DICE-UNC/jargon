/**
 *
 */
package edu.sdsc.jargon.testutils.icommandinvoke.icommands;

import java.util.ArrayList;
import java.util.List;

import edu.sdsc.jargon.testutils.icommandinvoke.IcommandException;

/**
 * @author Mike Conway, DICE (www.irods.org)
 *
 */
public class IchksumCommand implements Icommand {

	private String irodsFileName = "";

	public String getIrodsFileName() {
		return irodsFileName;
	}

	public void setIrodsFileName(String irodsFileName) {
		this.irodsFileName = irodsFileName;
	}

	/* (non-Javadoc)
	 * @see org.irods.jargon.icommandinvoke.icommands.Icommand#buildCommand()
	 */
	public List<String> buildCommand() throws IcommandException{
		if (irodsFileName.length() <= 0) {
			throw new IcommandException("no irods file name provided");
		}
		
		List<String> checksumCommand = new ArrayList<String>();
		checksumCommand.add("ichksum");
		
		checksumCommand.add(irodsFileName);
		return checksumCommand;
	}

	

}
