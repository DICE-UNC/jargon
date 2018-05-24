/**
 *
 */
package org.irods.jargon.datautils.filearchive;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for a utility to create an archive from a directory (recursively)
 *
 * @author Mike Conway - DICE
 *
 */
public abstract class AbstractArchiver {

	public static final Logger log = LoggerFactory.getLogger(AbstractArchiver.class);

	private final String sourceFileAbsolutePath;
	private final String targetFileAbsolutePath;

	/**
	 * Create an instance of an archiver with the source of the archive and the
	 * target path of the archive
	 *
	 * @param sourceFileAbsolutePath
	 *            {@code String} with an absolute path to the source file
	 * @param targetFileAbsolutePath
	 *            {@code String} with the absolute path to the target file
	 */
	public AbstractArchiver(final String sourceFileAbsolutePath, final String targetFileAbsolutePath) {

		if (sourceFileAbsolutePath == null || sourceFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty sourceFileAbsolutePath");
		}

		if (targetFileAbsolutePath == null || targetFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty targetFileAbsolutePath");
		}

		this.sourceFileAbsolutePath = sourceFileAbsolutePath;
		this.targetFileAbsolutePath = targetFileAbsolutePath;

	}

	/**
	 * Create an archive from the provided source file. This may be a file or a
	 * collection
	 *
	 * @return {@link File}
	 *
	 * @throws FileNotFoundException
	 *             {@link FileNotFoundException}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public File createArchive() throws FileNotFoundException, JargonException {

		log.info("createArchive()");

		File sourceFile = new File(sourceFileAbsolutePath);

		log.info("getting source file");

		if (!sourceFile.exists()) {
			throw new FileNotFoundException("source file does not exist");
		}

		log.info("file exists");

		initializeTargetArchive();

		if (sourceFile.isFile()) {
			log.info("this is a single file, archive it");
			return archiveSingleFile(sourceFile);
		} else {
			return archiveCollection(sourceFile);
		}
	}

	/**
	 * Given a directory that is the source for an archive, bundle the files
	 * contained
	 *
	 * @param sourceFile
	 *            {@link File} that is the source for the archive, this will be a
	 *            collection
	 * @return {@link File} that is the completed bundle
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	protected File archiveCollection(final File sourceFile) throws JargonException {

		log.info("creating iterator for collection");
		Iterator<File> fileIter = FileUtils.iterateFiles(sourceFile, TrueFileFilter.TRUE, TrueFileFilter.TRUE);

		while (fileIter.hasNext()) {
			addFileToArchive(fileIter.next());
		}

		log.info("done!");

		return completeArchiving();

	}

	/**
	 * Client should implement this method to signal the end of source files,
	 * triggering the completion of the archive
	 *
	 * @return {@link File} that is the completed archive, ready to use
	 *
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	protected abstract File completeArchiving() throws JargonException;

	/**
	 * Client should implement this method to add the given source file to the
	 * target archive
	 *
	 * @param file
	 *            {@link File} that is the source for the archive
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	protected abstract void addFileToArchive(final File file) throws JargonException;

	/**
	 * When the source is a single file, as opposed to a collection, this method
	 * should handle it
	 *
	 * @param sourceFile
	 *            {@link File} that is the source for the archive
	 * @return {@link File} that is the completed bundle
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	protected File archiveSingleFile(final File sourceFile) throws JargonException {
		log.info("adding file to archive");
		addFileToArchive(sourceFile);
		log.info("done!");

		return completeArchiving();
	}

	/**
	 * Subclasses should create whatever instance level objects needed to produce
	 * the archive
	 *
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	protected abstract void initializeTargetArchive() throws JargonException;

	public String getTargetFileAbsolutePath() {
		return targetFileAbsolutePath;
	}

	public String getSourceFileAbsolutePath() {
		return sourceFileAbsolutePath;
	}

}
