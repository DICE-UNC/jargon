package org.irods.jargon.datautils.connectiontester;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;

/**
 * Interface for a service to run connection tests, essentially doing a
 * configurable set of gets and puts of different sizes to see how fast and how
 * successful they are.
 *
 * @author Mike Conway - DICE
 *
 */
public interface ConnectionTester {

	public abstract ConnectionTestResult runTests(final List<TestType> testTypes)
			throws JargonException;

	public enum TestType {
		SMALL, MEDIUM, LARGE, EXTRA_LARGE
	}

}