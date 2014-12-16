/**
 * 
 */
package org.irods.jargon.datautils.connectiontester;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;

/**
 * Report of the result of a test
 * 
 * @author Mike Conway - DICE
 * 
 */
public class ConnectionTestResult {

	/**
	 * The configuration for this test
	 */
	private ConnectionTesterConfiguration configuration;
	/**
	 * IRODSAccount used for the test
	 */
	private IRODSAccount irodsAccount;
	/**
	 * A list of results by test type requested
	 */
	private List<ConnectionTestResult> testResults = new ArrayList<ConnectionTestResult>();

	/**
	 * 
	 */
	public ConnectionTestResult() {
	}

	/**
	 * @return the configuration
	 */
	public ConnectionTesterConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration
	 *            the configuration to set
	 */
	public void setConfiguration(ConnectionTesterConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * @return the irodsAccount
	 */
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	/**
	 * @param irodsAccount
	 *            the irodsAccount to set
	 */
	public void setIrodsAccount(IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	/**
	 * @return the testResults
	 */
	public List<ConnectionTestResult> getTestResults() {
		return testResults;
	}

	/**
	 * @param testResults
	 *            the testResults to set
	 */
	public void setTestResults(List<ConnectionTestResult> testResults) {
		this.testResults = testResults;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("ConnectionTestResult [");
		if (configuration != null) {
			builder.append("configuration=");
			builder.append(configuration);
			builder.append(", ");
		}
		if (irodsAccount != null) {
			builder.append("irodsAccount=");
			builder.append(irodsAccount);
			builder.append(", ");
		}
		if (testResults != null) {
			builder.append("testResults=");
			builder.append(testResults.subList(0,
					Math.min(testResults.size(), maxLen)));
		}
		builder.append("]");
		return builder.toString();
	}

}
