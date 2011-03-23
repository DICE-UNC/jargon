package org.irods.jargon.transfer.engine.synch.scriptdriver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * * TODO: under construction... Functional test driver framework to read a
 * little script and fire off appropriate handlers. This supports a 'little
 * language' that can be used to drive functional tests and stress tests.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ScriptDriver {

	private final Map<String, IRODSAccount> irodsAccounts = new HashMap<String, IRODSAccount>();
	private final Properties properties = new Properties();
	private final IRODSFileSystem irodsFileSystem;
	private final Map<String, AbstractScriptHandler> scriptHandlers = new HashMap<String, AbstractScriptHandler>();
	private final File scriptDriverFile;
	private boolean running = true;
	private List<String> scriptLines = new ArrayList<String>();
	private int scriptPointer = 0;

	public static final Logger log = LoggerFactory
			.getLogger(ScriptDriver.class);

	/**
	 * Create an instance of the script driver for a given file.
	 * 
	 * @param irodsFileSystem
	 * @param scriptDriverFile
	 * @throws ScriptingException
	 */
	public ScriptDriver(final IRODSFileSystem irodsFileSystem,
			final File scriptDriverFile) throws ScriptingException {
		this.irodsFileSystem = irodsFileSystem;
		this.scriptDriverFile = scriptDriverFile;
	}

	public synchronized IRODSFileSystem getIrodsFileSystem() {
		return irodsFileSystem;
	}

	public synchronized void start() throws ScriptingException {
		running = true;
		runScript();
	}

	public synchronized void end() {
		running = false;
	}

	public synchronized void addHandler(final String handlerName,
			final AbstractScriptHandler handler) throws ScriptingException {
		scriptHandlers.put(handlerName, handler);
	}

	public synchronized void runScript() throws ScriptingException {

		// bounce through the script lines, which have been trimmed of all but
		// calls to handlers
		String line;
		String lineCommand;
		while (running && scriptPointer < scriptLines.size()) {
			line = scriptLines.get(scriptPointer);
			log.info("processing script line:{}", line);
			lineCommand = parseLineCommand(line);
			AbstractScriptHandler handler = scriptHandlers.get(lineCommand);
			if (handler == null) {
				log.error("cannot find a handler for:{}", lineCommand);
			}

		}
	}

	String parseLineCommand(final String line) throws ScriptingException {

		if (line == null || line.isEmpty()) {
			throw new ScriptingException("null or empty line");
		}

		String[] lineVals = line.split("[|]");
		if (lineVals.length < 1) {
			throw new ScriptingException("no data in line");
		}

		return lineVals[0];

	}

}
