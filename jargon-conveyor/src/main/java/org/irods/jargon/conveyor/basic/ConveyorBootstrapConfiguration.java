/**
 * 
 */
package org.irods.jargon.conveyor.basic;

/**
 * Configuration data for the <code>BasicConveyorBootstrapper</code> that will
 * configure the delivered <code>BasicConveyorService</code>. A simple value
 * object.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ConveyorBootstrapConfiguration {

	private String passPhrase = null;
	private String databaseURL = null;
	private String databaseUser = null;
	private String databasePassword = null;

	public String getPassPhrase() {
		return passPhrase;
	}

	public void setPassPhrase(final String passPhrase) {
		this.passPhrase = passPhrase;
	}

	public String getDatabaseURL() {
		return databaseURL;
	}

	public void setDatabaseURL(final String databaseURL) {
		this.databaseURL = databaseURL;
	}

	public String getDatabaseUser() {
		return databaseUser;
	}

	public void setDatabaseUser(final String databaseUser) {
		this.databaseUser = databaseUser;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void setDatabasePassword(final String databasePassword) {
		this.databasePassword = databasePassword;
	}

	/**
	 * 
	 */
	public ConveyorBootstrapConfiguration() {

	}

}
