package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.IRodsPI;

/**
 * Interface for generic handling of ApiPlugins, which typically take a JSON
 * object as input and return a JSON structure. JSON serialization and
 * deserialization is external to this process
 * 
 * @author conwaymc
 *
 */
interface ApiPluginExecutor {

	/**
	 * Make a function call using a regular packing instruction as a request and
	 * expecting JSON as a result
	 * 
	 * @param apiNumber {@code int} with the API number
	 * @param irodsPI   {@link IRodsPI}
	 * @return a {@code String} with serialized JSON
	 * @throws JargonException {@link JargonException} for upstream iRODS errors
	 */
	PluggableApiCallResult callPluggableApi(int apiNumber, IRodsPI irodsPI) throws JargonException;

	/**
	 * Make a function call using a serialized JSON request and expecting JSON as a
	 * result
	 * 
	 * @param apiNumber {@code int} with the API number
	 * @param request   {@code String}
	 * @return a {@code String} with serialized JSON
	 * @throws JargonException {@link JargonException} for upstream iRODS errors
	 */
	PluggableApiCallResult callPluggableApi(int apiNumber, String request) throws JargonException;

}