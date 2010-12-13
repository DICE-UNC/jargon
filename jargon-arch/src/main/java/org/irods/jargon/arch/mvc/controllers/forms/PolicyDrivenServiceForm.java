/**
 * 
 */
package org.irods.jargon.arch.mvc.controllers.forms;

/**
 * Form for policy driven service add
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class PolicyDrivenServiceForm {
	private String serviceName = "";
	private String serviceRootPath = "";
	private String comment = "";
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServiceRootPath() {
		return serviceRootPath;
	}
	public void setServiceRootPath(String serviceRootPath) {
		this.serviceRootPath = serviceRootPath;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("policyDrivenServiceForm:");
		sb.append("\n   serviceName:");
		sb.append(serviceName);
		sb.append("\n   serviceRootPath:");
		sb.append(serviceRootPath);
		sb.append("\n   comment:");
		sb.append(comment);
		return sb.toString();
	}
	
	

}
