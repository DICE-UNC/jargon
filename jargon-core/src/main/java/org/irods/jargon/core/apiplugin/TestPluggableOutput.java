/**
 * 
 */
package org.irods.jargon.core.apiplugin;

/**
 * Test output class for pluggable api
 * 
 * @author conwaymc
 *
 */
public class TestPluggableOutput {

	private String string1 = "string1";
	private int int1 = 1;

	/**
	 * 
	 */
	public TestPluggableOutput() {
	}

	public String getString1() {
		return string1;
	}

	public void setString1(String string1) {
		this.string1 = string1;
	}

	public int getInt1() {
		return int1;
	}

	public void setInt1(int int1) {
		this.int1 = int1;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestPluggableOutput [");
		if (string1 != null) {
			builder.append("string1=").append(string1).append(", ");
		}
		builder.append("int1=").append(int1).append("]");
		return builder.toString();
	}

}
