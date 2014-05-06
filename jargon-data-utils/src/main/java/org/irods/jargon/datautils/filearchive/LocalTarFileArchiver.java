/**
 * 
 */
package org.irods.jargon.datautils.filearchive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archiver that will tar a given local collection
 * 
 * @author Mike Conway - DICE
 * 
 */
public class LocalTarFileArchiver extends AbstractArchiver {

	private File tarArchiveFile = null;
	private TarArchiveOutputStream tarArchiveOutputStream = null;

	public static final Logger log = LoggerFactory
			.getLogger(LocalTarFileArchiver.class);

	/**
	 * @param sourceFileAbsolutePath
	 * @param targetFileAbsolutePath
	 */
	public LocalTarFileArchiver(String sourceFileAbsolutePath,
			String targetFileAbsolutePath) {
		super(sourceFileAbsolutePath, targetFileAbsolutePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.datautils.filearchive.AbstractArchiver#completeArchiving
	 * ()
	 */
	@Override
	protected File completeArchiving() throws JargonException {
		log.info("complete archiving");
		try {
			tarArchiveOutputStream.flush();
			tarArchiveOutputStream.close();
			return this.tarArchiveFile;

		} catch (IOException e) {
			log.error("io exception in completeArchiving", e);
			throw new JargonException("io exception in completeArchiving", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.datautils.filearchive.AbstractArchiver#addFileToArchive
	 * (java.io.File)
	 */
	@Override
	protected void addFileToArchive(File file) throws JargonException {

		log.info("adding file to tar:{}", file);

		TarArchiveEntry entry = new TarArchiveEntry(file.getAbsolutePath());
		entry.setSize(file.length());
		try {
			tarArchiveOutputStream.putArchiveEntry(entry);
			InputStream fileIn = new BufferedInputStream(new FileInputStream(
					file));
			IOUtils.copy(fileIn, tarArchiveOutputStream);
			tarArchiveOutputStream.closeArchiveEntry();
			fileIn.close();
		} catch (FileNotFoundException e) {
			log.error("file not found in copy to tar", e);
			throw new JargonException("file not found in copy to tar", e);
		} catch (IOException e) {
			log.error("io exception in copy to tar", e);
			throw new JargonException("io exception in copy to tar", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.datautils.filearchive.AbstractArchiver#
	 * initializeTargetArchive()
	 */
	@Override
	protected void initializeTargetArchive() throws JargonException {
		log.info("initializeTargetArchive()");
		tarArchiveFile = new File(this.getTargetFileAbsolutePath());
		log.info("tar target file:{}", tarArchiveFile.getAbsolutePath());

		if (tarArchiveFile.exists()) {
			tarArchiveFile.delete();
		}

		log.info("creating tar archive stream from file");

		try {
			FileOutputStream fos = new FileOutputStream(tarArchiveFile);

			this.tarArchiveOutputStream = (TarArchiveOutputStream) new ArchiveStreamFactory()
					.createArchiveOutputStream(ArchiveStreamFactory.TAR, fos);
			tarArchiveOutputStream
					.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

		} catch (FileNotFoundException e) {
			log.error("fileNotFoundException initializing target archive", e);
			throw new JargonException("cannot find target file");
		} catch (ArchiveException e) {
			log.error("archiveExcpetion creating archive for tar", e);
			throw new JargonException("archiveexception creating archive type",
					e);
		}

	}

}
