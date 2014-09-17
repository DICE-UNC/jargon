/**
 * 
 */
package org.irods.jargon.datautils.tree;

import java.io.File;
import java.io.FileNotFoundException;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.service.AbstractJargonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to generate a summary of the contents of a file tree. This will
 * iterate over the files and get averages, max and min length, counts and two
 * tables, one that gives file length buckets to get a basic 'histogram' of the
 * relative frequency of each size, and a table that gives counts by file
 * extension.
 * <p/>
 * This service handles both local and iRODS directories
 * 
 * @author Mike Conway - DICE
 * 
 * 
 */
public class TreeSummarizingServiceImpl extends AbstractJargonService implements
		TreeSummarizingService {

	private static final Logger log = LoggerFactory
			.getLogger(TreeSummarizingServiceImpl.class);

	/**
	 * @param irodsAccessObjectFactory
	 * @param irodsAccount
	 */
	public TreeSummarizingServiceImpl(
			final IRODSAccessObjectFactory irodsAccessObjectFactory,
			final IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.tree.TreeSummarizingService#
	 * generateTreeSummaryForIrodsFileTree(java.lang.String)
	 */
	@Override
	public TreeSummary generateTreeSummaryForIrodsFileTree(
			final String irodsFilePath) throws FileNotFoundException,
			JargonException {
		log.info("generateTreeSummaryForIrodsFileTree()");

		if (irodsFilePath == null || irodsFilePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty localFilePath");
		}

		File irodsFile = (File) getIrodsAccessObjectFactory()
				.getIRODSFileFactory(getIrodsAccount()).instanceIRODSFile(
						irodsFilePath);
		if (!irodsFile.exists()) {
			throw new FileNotFoundException("cannot find local file");
		}

		TreeSummarizingVisitor fileTreeIteratorVisitor = new TreeSummarizingVisitor();
		FileTreeIteratorVisitorInvoker invoker = new FileTreeIteratorVisitorInvoker(
				getIrodsAccessObjectFactory(), getIrodsAccount(),
				fileTreeIteratorVisitor, irodsFile);

		log.info("executing...");

		invoker.execute();
		log.info("...done");
		TreeSummary summary = fileTreeIteratorVisitor.getTreeSummary();
		return summary;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.tree.TreeSummarizingService#
	 * generateTreeSummaryForLocalFileTree(java.lang.String)
	 */
	@Override
	public TreeSummary generateTreeSummaryForLocalFileTree(
			final String localFilePath) throws FileNotFoundException,
			JargonException {

		log.info("generateTreeSummaryForLocalFileTree()");

		if (localFilePath == null || localFilePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty localFilePath");
		}

		File localPathFile = new File(localFilePath);
		if (!localPathFile.exists()) {
			throw new FileNotFoundException("cannot find local file");
		}

		TreeSummarizingVisitor fileTreeIteratorVisitor = new TreeSummarizingVisitor();
		FileTreeIteratorVisitorInvoker invoker = new FileTreeIteratorVisitorInvoker(
				getIrodsAccessObjectFactory(), getIrodsAccount(),
				fileTreeIteratorVisitor, localPathFile);

		log.info("executing...");

		invoker.execute();
		log.info("...done");
		TreeSummary summary = fileTreeIteratorVisitor.getTreeSummary();
		return summary;

	}

}
