package org.irods.jargon.transfer.engine.synch.scriptdriver;

import java.util.Map;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;

/**
 * TODO: under construction... Basic facility to define a handler that responds
 * to a script command. These handlers are registered by command name and will
 * be called with the text of the command, as well as the properties that have
 * been configured in the script handler.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractScriptHandler {

	private Map<String, IRODSAccount> irodsAccounts;
	private Properties properties;
	private IRODSFileSystem irodsFileSystem;

	public AbstractScriptHandler(final Map<String, IRODSAccount> irodsAccounts,
			final Properties properties, final IRODSFileSystem irodsFileSystem) {
		super();
		this.irodsAccounts = irodsAccounts;
		this.properties = properties;
		this.irodsFileSystem = irodsFileSystem;
	}

	public Map<String, IRODSAccount> getIrodsAccounts() {
		return irodsAccounts;
	}

	public Properties getProperties() {
		return properties;
	}

	public IRODSFileSystem getIrodsFileSystem() {
		return irodsFileSystem;
	}

	/**
	 * Given the
	 * 
	 * @param scriptCommand
	 * @throws ScriptingException
	 * @throws JargonException
	 */
	public abstract void execute(final String scriptCommand)
			throws ScriptingException, JargonException;

}
