/**
 * 
 */
package org.irods.jargon.core.apiplugin;

/**
 * Abstract for a plugin api request, to be subclassed by a particular instance
 * per implementation
 * 
 * @author conwaymc
 *
 */
public abstract class PluginApiRequest {

	// api number
	// <AtomicMetadataOperation, AtomicMetadataResponse>
	// AtomicMetadataResponse resp = execApi()

	/**
	 * 
	 */
	public PluginApiRequest() {
	}

}
