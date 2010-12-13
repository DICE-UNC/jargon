/**
 * 
 */
package org.irods.jargon.arch.mvc.controllers.forms;

/**
 * Backing form for add of policy repository.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class PolicyRepositoryForm {

	private String repositoryName = "";
	private String repositoryPath = "";
	private String comment = "";

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("RepositoryForm:");
		sb.append("\n   repositoryName:");
		sb.append(repositoryName);
		sb.append("\n   repositoryPath:");
		sb.append(repositoryPath);
		sb.append("\n   comment:");
		sb.append(comment);
		return sb.toString();
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public String getRepositoryPath() {
		return repositoryPath;
	}

	public void setRepositoryPath(String repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

}
