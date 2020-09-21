/**
 * 
 */
package org.irods.jargon.core.apiplugin;

/**
 * Test class to serve as input to a pluggable api
 * 
 * @author conwaymc
 *
 */
public class TestPluggableInput {

	private String string1 = "string1";
	private String string2 = "string2";
	private int int1 = 1;
	private boolean bool1 = false;

	/**
	 * 
	 */
	public TestPluggableInput() {
	}

	public String getString1() {
		return string1;
	}

	public void setString1(String string1) {
		this.string1 = string1;
	}

	public String getString2() {
		return string2;
	}

	public void setString2(String string2) {
		this.string2 = string2;
	}

	public int getInt1() {
		return int1;
	}

	public void setInt1(int int1) {
		this.int1 = int1;
	}

	public boolean isBool1() {
		return bool1;
	}

	public void setBool1(boolean bool1) {
		this.bool1 = bool1;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TestPluggableInput [");
		if (string1 != null) {
			builder.append("string1=").append(string1).append(", ");
		}
		if (string2 != null) {
			builder.append("string2=").append(string2).append(", ");
		}
		builder.append("int1=").append(int1).append(", bool1=").append(bool1).append("]");
		return builder.toString();
	}

}
