/**
 *
 */
package edu.sdsc.jargon.testutils.icommandinvoke.icommands;
import java.util.List;

import edu.sdsc.jargon.testutils.icommandinvoke.IcommandException;

/**
 * Handles the invocation of an iCommand on the underlying system
 * @author Mike Conway
 *
 */
public interface Icommand {
	
	public List<String> buildCommand() throws IcommandException;

}
