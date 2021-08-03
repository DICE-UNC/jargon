package org.irods.jargon.core.pub;

public class PluggableApiCallResult {

	private String jsonResult = "";
	private int intInfo = 0;
	private int errorInfo = 0;

	public String getJsonResult() {
		return jsonResult;
	}

	public void setJsonResult(String jsonResult) {
		this.jsonResult = jsonResult;
	}

	public int getIntInfo() {
		return intInfo;
	}

	public void setIntInfo(int intInfo) {
		this.intInfo = intInfo;
	}

	public int getErrorInfo() {
		return errorInfo;
	}

	public void setErrorInfo(int errorInfo) {
		this.errorInfo = errorInfo;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PluggableApiCallResult [");
		if (jsonResult != null) {
			builder.append("jsonResult=").append(jsonResult).append(", ");
		}
		builder.append("intInfo=").append(intInfo).append(", errorInfo=").append(errorInfo).append("]");
		return builder.toString();
	}

}
