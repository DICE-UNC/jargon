/**
 *
 */
package edu.sdsc.jargon.testutils.icommandinvoke;

/**
 * Describes properties for connection to an irods server
 * @author Mike Conway, DICE (www.irods.org)
 * @since 10/16/2009
 */
public class IrodsInvocationContext {
	private String irodsHost = "";
	private int irodsPort = 1247;
	private String irodsZone = "";
	private String localWorkingDirectory = "";
	private String irodsUser = "";
	private String irodsPassword = "";
	private String irodsScratchDir = "";
	private String irodsResource = "";
	
	public IrodsInvocationContext() {
		
	}
	
	public String getIrodsResource() {
		return irodsResource;
	}
	public void setIrodsResource(String irodsResource) {
		this.irodsResource = irodsResource;
	}
	public String getIrodsScratchDir() {
		return irodsScratchDir;
	}
	public void setIrodsScratchDir(String irodsScratchDir) {
		this.irodsScratchDir = irodsScratchDir;
	}
	
	/**
	 * @return the irodsHost
	 */
	public String getIrodsHost() {
		return irodsHost;
	}
	/**
	 * @param irodsHost the irodsHost to set
	 */
	public void setIrodsHost(String irodsHost) {
		this.irodsHost = irodsHost;
	}
	/**
	 * @return the irodsPort
	 */
	public int getIrodsPort() {
		return irodsPort;
	}
	/**
	 * @param irodsPort the irodsPort to set
	 */
	public void setIrodsPort(int irodsPort) {
		this.irodsPort = irodsPort;
	}
	/**
	 * @return the irodsZone
	 */
	public String getIrodsZone() {
		return irodsZone;
	}
	/**
	 * @param irodsZone the irodsZone to set
	 */
	public void setIrodsZone(String irodsZone) {
		this.irodsZone = irodsZone;
	}
	/**
	 * @return the localWorkingDirectory
	 */
	public String getLocalWorkingDirectory() {
		return localWorkingDirectory;
	}
	/**
	 * @param localWorkingDirectory the localWorkingDirectory to set
	 */
	public void setLocalWorkingDirectory(String localWorkingDirectory) {
		this.localWorkingDirectory = localWorkingDirectory;
	}
	/**
	 * @return the irodsUser
	 */
	public String getIrodsUser() {
		return irodsUser;
	}
	/**
	 * @param irodsUser the irodsUser to set
	 */
	public void setIrodsUser(String irodsUser) {
		this.irodsUser = irodsUser;
	}
	/**
	 * @return the irodsPassword
	 */
	public String getIrodsPassword() {
		return irodsPassword;
	}
	/**
	 * @param irodsPassword the irodsPassword to set
	 */
	public void setIrodsPassword(String irodsPassword) {
		this.irodsPassword = irodsPassword;
	}
	
	
	
}
