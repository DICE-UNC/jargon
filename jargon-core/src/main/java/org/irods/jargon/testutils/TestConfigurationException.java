/**
 *
 */
package org.irods.jargon.testutils;

/**
 * General exception caused by using Test utilities
 *
 * @author Mike Conway, DICE (10/16/2009)
 * @since 10/16/2009
 *
 */
public class TestConfigurationException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 2124699560973645080L;

	/**
	 *
	 */
	public TestConfigurationException() {
		super();
	}

	/**
	 * @param arg0
	 */
	public TestConfigurationException(final String arg0) {
		super(arg0);

	}

	/**
	 * @param arg0
	 */
	public TestConfigurationException(final Throwable arg0) {
		super(arg0);

	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public TestConfigurationException(final String arg0, final Throwable arg1) {
		super(arg0, arg1);

	}

}
