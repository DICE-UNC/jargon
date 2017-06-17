package org.irods.jargon.datautils.tree;

import java.io.FileNotFoundException;

import org.irods.jargon.core.exception.JargonException;

/**
 * Interface for a service to iterate recursively through files in a directory
 * tree, creating an overall summary that gives statistics about file sizes,
 * etc, via the {@link TreeSummary}. This service can work with both local and
 * iRODS files.
 * 
 * @author Mike Conway - DICE
 * 
 */
public interface TreeSummarizingService {

	/**
	 * Create a summary describing the contents of an iRODS file tree
	 * 
	 * @param irodsFilePath
	 *            {@code String} with an absolute path to an iRODS tree
	 * @return {@link TreeSummary}
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	TreeSummary generateTreeSummaryForIrodsFileTree(String irodsFilePath)
			throws FileNotFoundException, JargonException;

	/**
	 * Create a summary describing the contents of a local file tree
	 * 
	 * @param irodsFilePath
	 *            {@code String} with an absolute path to a local file
	 *            system tree
	 * @return {@link TreeSummary}
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	TreeSummary generateTreeSummaryForLocalFileTree(String localFilePath)
			throws FileNotFoundException, JargonException;

}