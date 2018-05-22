/**
 * 
 */
package org.irods.jargon.datautils.filearchive;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archiver that will tar a given local collection
 * 
 * @author Mike Conway - DICE
 * 
 */
public class LocalFileGzipCompressor {

	public static final Logger log = LoggerFactory.getLogger(LocalFileGzipCompressor.class);

	/**
	 * Take the given file and compress it to a gzip with a .gzip extension added
	 * 
	 * @param inputFileAbsolutePath
	 *            {@code String} with an absolute path to a file that is gzip
	 *            compressed
	 * @return {@link File} with the compressed gzip
	 * @throws FileNotFoundException
	 *             {@link FileNotFoundException}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public File compress(final String inputFileAbsolutePath) throws FileNotFoundException, JargonException {

		log.info("compress");

		if (inputFileAbsolutePath == null || inputFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty inputFileAbsolutePath");
		}

		log.info("inputFileAbsolutePath:{}", inputFileAbsolutePath);

		try {

			File inputFile = new File(inputFileAbsolutePath);
			if (!inputFile.exists()) {
				throw new FileNotFoundException("unable to find input file");
			}

			StringBuilder sb = new StringBuilder();
			sb.append(inputFile.getName());
			sb.append(".gzip");

			File outputFile = new File(inputFile.getParent(), sb.toString());
			log.info("output file is:{}", outputFile);

			log.info("creating input stream...");
			InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));

			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

			log.info("creating gzip streams..");
			GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(outputStream);

			log.info("stream copy");
			IOUtils.copy(inputStream, gzOut);
			inputStream.close();
			gzOut.close();

			log.info("done!");
			return outputFile;

		} catch (IOException e) {
			log.error("io exception in completeArchiving", e);
			throw new JargonException("io exception in completeArchiving", e);
		}

	}

	/**
	 * Given a file, unzip it.
	 * <p>
	 * Note the result file will either be the same name with the .gzip removed,
	 * leaving the original extension. If a .gzip extension is not found it will
	 * make it a .tar file by default
	 * 
	 * @param inputFileAbsolutePath
	 *            {@code String} with the absolute path to a .gzip file
	 * @return {@link File} that is unzipped and has the .gzip removed to give the
	 *         original extension, or if it can't figure it out it makes a tar
	 * @throws FileNotFoundException
	 *             {@link FileNotFoundException}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	public File uncompress(final String inputFileAbsolutePath) throws FileNotFoundException, JargonException {

		log.info("uncompressToTar()");

		if (inputFileAbsolutePath == null || inputFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty inputFileAbsolutePath");
		}

		log.info("inputFileAbsolutePath:{}", inputFileAbsolutePath);

		try {

			File inputFile = new File(inputFileAbsolutePath);
			if (!inputFile.exists()) {
				throw new FileNotFoundException("unable to find input file");
			}

			int idxOfDotGzip = inputFile.getName().indexOf(".gzip");

			String fileName;
			if (idxOfDotGzip == -1) {
				fileName = LocalFileUtils.getFileNameUpToExtension(inputFile.getName());

				StringBuilder sb = new StringBuilder();
				sb.append(fileName);
				sb.append(".tar");
				fileName = sb.toString();
			} else {
				fileName = inputFile.getName().substring(0, idxOfDotGzip);
			}

			File outputFile = new File(inputFile.getParent(), fileName);
			log.info("output file is:{}", outputFile);

			log.info("creating input stream...");
			InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));

			GzipCompressorInputStream gzIn = new GzipCompressorInputStream(inputStream);

			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

			log.info("stream copy");
			IOUtils.copy(gzIn, outputStream);
			gzIn.close();
			outputStream.close();

			log.info("done!");
			return outputFile;

		} catch (IOException e) {
			log.error("io exception in completeArchiving", e);
			throw new JargonException("io exception in completeArchiving", e);
		}

	}

}
