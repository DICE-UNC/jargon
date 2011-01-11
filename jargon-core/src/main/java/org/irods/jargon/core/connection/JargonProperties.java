package org.irods.jargon.core.connection;

import org.irods.jargon.core.exception.JargonException;

/**
 * Represents a source of configuration metadata that will effect the behavior
 * of Jargon through a properties file or other configuration source.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface JargonProperties {

	/**
	 * Do I want parallel transfers at all?
	 * 
	 * @return
	 * @throws JargonException
	 */
	boolean isUseParallelTransfer() throws JargonException;

	/**
	 * If doing paralell transfers, what is the maximum number of threads I
	 * should specify?
	 * 
	 * @return
	 * @throws JargonException
	 */
	int getMaxParallelThreads() throws JargonException;

}