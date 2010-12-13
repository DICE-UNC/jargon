
package org.irods.jargon.lingo.mvc.controllers.forms;

import java.util.ArrayList;
import java.util.List;

/**
 * Form for user add operation
 * 
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class UserAddForm {

	private String name;
	private String password;
	private String confirmPassword;
	private String userType;
	private List<String> userGroup = new ArrayList<String>();
	private String userDN;
	private String userZone;
	private String info;
	private String comment;

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final String getPassword() {
		return password;
	}

	public final void setPassword(String password) {
		this.password = password;
	}

	public final String getConfirmPassword() {
		return confirmPassword;
	}

	public final void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public final String getUserType() {
		return userType;
	}

	public final void setUserType(String userType) {
		this.userType = userType;
	}

	public final List<String> getUserGroup() {
		return userGroup;
	}

	public final void setUserGroup(List<String> userGroup) {
		this.userGroup = userGroup;
	}

	public final String getUserDN() {
		return userDN;
	}

	public final void setUserDN(String userDN) {
		this.userDN = userDN;
	}

	public final String getInfo() {
		return info;
	}

	public final void setInfo(String info) {
		this.info = info;
	}

	public final String getComment() {
		return comment;
	}

	public final void setComment(String comment) {
		this.comment = comment;
	}

	public final String getUserZone() {
		return userZone;
	}

	public final void setUserZone(String userZone) {
		this.userZone = userZone;
	}
	
	
}
