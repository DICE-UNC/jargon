/**
 *
 */
package org.irods.jargon.testutils.filemanip;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.irods.jargon.testutils.TestConfigurationException;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helper methods to generate dummy files and directories useful for Jargon
 * testing
 *
 * @author Mike Conway, DICE (www.irods.org)
 * @since 10/16/2009
 *
 */
public class FileGenerator {

	public static List<String> fileExtensions = new ArrayList<String>();
	private static final Random RANDOM = new Random();
	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();

	private static Logger log = LogManager.getLogger(FileGenerator.class);

	static {
		fileExtensions.add(".doc");
		fileExtensions.add(".txt");
		fileExtensions.add(".csv");
		fileExtensions.add(".gif");
		fileExtensions.add(".jpg");
		fileExtensions.add(".avi");
		try {
			testingProperties = testingPropertiesHelper.getTestProperties();
		} catch (TestConfigurationException e) {
			throw new IllegalStateException("cannot find testing properties", e);
		}
	}

	/**
	 * Create a random file extension selected from the {@code fileExtensions}
	 * options
	 *
	 * @return {@code String} containing a random, though valid, file extension
	 *         prepended with a '.' character
	 * @throws TestConfigurationException
	 *             {@link TestConfigurationException}
	 */
	protected static String generateRandomExtension() throws TestConfigurationException {
		return fileExtensions.get(generateRandomNumber(0, fileExtensions.size()));
	}

	/**
	 * Generate a random string
	 *
	 * @param length
	 *            {@code int} that determines the length of the {@code String} that
	 *            is generated.
	 * @return {@code String} of n length composed of random alphabetic characters
	 */
	public static String generateRandomString(final int length) {

		StringBuilder outString = new StringBuilder();
		Random generator = new Random();
		char theChar = 0;
		int outLength = 0;

		while (outLength < length) {
			theChar = (char) (generator.nextInt(65) + 65);
			if (Character.isLetter(theChar)) {
				outString.append(theChar);
				outLength++;
			}

		}
		return outString.toString();
	}

	/**
	 * Generate a random file name + extension
	 *
	 * @param length
	 *            {@code int} that determines the length of the file name (not
	 *            including the extension this will be appended)
	 * @return {@code String} which is a random file name plus a random extension
	 * @throws TestConfigurationException
	 *             {@link TestConfigurationException}
	 */
	protected static String generateRandomFileName(final int length) throws TestConfigurationException {
		StringBuilder fileName = new StringBuilder();
		fileName.append(generateRandomString(length));
		fileName.append(generateRandomExtension());
		return fileName.toString();
	}

	/**
	 * Given a directory, generate a randomly named file with random data of
	 * provided length.
	 *
	 * @param fileDirectory
	 *            {@code String} containing a an absolute path to valid directory on
	 *            the local file system. This absolute path must have a leading and
	 *            trailing '/'.
	 * @param fileName
	 *            {@code String} with file name
	 * @param length
	 *            {@code long} containing the desired length of the file in bytes
	 * @return {@code String} containing the full path to the generated file
	 * @throws TestConfigurationException
	 *             {@link TestConfigurationException}
	 */
	public static String generateFileOfFixedLengthGivenName(final String fileDirectory, final String fileName,
			final long length) throws TestConfigurationException {

		// 1023 bytes of random stuff should be plenty, then just repeat it as
		// needed, this is odd number to prevent lining up on even number buffer
		// offsets
		if (fileDirectory == null || fileDirectory.isEmpty()) {
			throw new IllegalArgumentException("null or empty FileDirectory");
		}

		if (fileName == null || fileName.isEmpty()) {
			throw new IllegalArgumentException("null or empty fileName");
		}

		File dir = new File(fileDirectory);
		dir.mkdirs();

		long chunkSize = 1023;
		if (length <= chunkSize) {
			chunkSize = length;
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int generatedLength = 0;

		while (generatedLength < chunkSize) {
			bos.write(RANDOM.nextInt());
			generatedLength += 1;
		}

		byte[] fileChunk = bos.toByteArray();

		// take the chunk and fill up the file
		File randFile = new File(fileDirectory, fileName);
		OutputStream outStream = null;

		long generatedFileLength = 0;
		long nextChunkSize = 0;
		try {
			outStream = new BufferedOutputStream(new FileOutputStream(randFile));

			while (generatedFileLength < length) {

				// if more than chunk size to go, just write the chunk
				nextChunkSize = length - generatedFileLength;
				if (nextChunkSize > chunkSize) {
					outStream.write(fileChunk);
					generatedFileLength += chunkSize;
				} else {
					outStream.write(fileChunk, 0, (int) nextChunkSize);
					generatedFileLength += nextChunkSize;
				}

			}

		} catch (IOException ioe) {
			throw new TestConfigurationException(
					"error generating random file with dir:" + fileDirectory + " and generated name:" + fileName, ioe);
		} finally {
			if (outStream != null) {
				try {

					outStream.close();
				} catch (Exception ex) {
					// ignore
				}
			}
		}

		StringBuilder fullPath = new StringBuilder();
		fullPath.append(fileDirectory);
		// fullPath.append("/");
		fullPath.append(fileName);
		return fullPath.toString();

	}

	public static int generateRandomNumber(final int min, final int max) throws TestConfigurationException {
		if (max < min) {
			throw new TestConfigurationException("max length must be > or = min length");
		}

		int range = max - min;
		if (range <= 0) {
			range = 2;
		}
		Random generator = new Random();
		return min + generator.nextInt(range);
	}

	/**
	 * Handy method to generate a tree of files and collections with given
	 * parameters. This method will recursively build a local file tree under a
	 * given absolute path. The collections are of random number within a range, to
	 * a given depth, and containing files and subcollections given the various size
	 * and range parameters. This method can provide a test-bed for various
	 * functional testing scenarios.
	 *
	 * @param absolutePathToLocalParentCollection
	 *            {@code String} with the abs path to the parent collection of the
	 *            generated subtree
	 * @param collectionPrefix
	 *            {@code String} with the first part of the collection name
	 * @param numberOfCollectionsMin
	 *            {@code int} with the min number of collections generated as
	 *            children
	 * @param numberOfCollectionsMax
	 *            {@code int} with the max number of collections generated as
	 *            children
	 * @param depth
	 *            {@code int} with the overall depth of the collection tree
	 * @param filePrefix
	 *            {@code String} with a file prefix for generated files
	 * @param fileSuffix
	 *            {@code String} with a file suffix for generated files
	 * @param maxNumberOfFiles
	 *            {@code int} with the max number of files generated as children
	 * @param minNumberOfFiles
	 *            {@code int} with the min number of files generated as children
	 * @param fileLengthMin
	 *            {@code int} min length in bytes of generated file
	 * @param fileLengthMax
	 *            {@code int} max length in bytes of generated file
	 * @throws TestConfigurationException
	 *             {@link TestConfigurationException}
	 */
	public static void generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(
			final String absolutePathToLocalParentCollection, final String collectionPrefix,
			final int numberOfCollectionsMin, final int numberOfCollectionsMax, final int depth,
			final String filePrefix, final String fileSuffix, final int maxNumberOfFiles, final int minNumberOfFiles,
			final int fileLengthMin, final int fileLengthMax) throws TestConfigurationException {

		int numberThisParent;
		if (numberOfCollectionsMin == numberOfCollectionsMax) {
			numberThisParent = numberOfCollectionsMin;
		} else {
			numberThisParent = generateRandomNumber(numberOfCollectionsMin, numberOfCollectionsMax);
		}

		if (depth == 0) {
			return;
		}

		File localFile;
		StringBuilder subdirName;
		StringBuilder absolutePath;
		for (int i = 0; i < numberThisParent; i++) {
			subdirName = new StringBuilder();

			// create a new subtree name
			subdirName.append(collectionPrefix);
			subdirName.append("lvl");
			subdirName.append(depth);
			subdirName.append("nbr");
			subdirName.append(i);
			subdirName.append('/');

			absolutePath = new StringBuilder();
			absolutePath.append(absolutePathToLocalParentCollection);
			absolutePath.append(subdirName);

			localFile = new File(absolutePath.toString());
			boolean success = localFile.mkdirs();

			if (!success) {
				log.warn("mkdirs for {} did not return success", localFile.getAbsolutePath());
			}

			int numberOfFiles;

			if (minNumberOfFiles == maxNumberOfFiles) {
				numberOfFiles = minNumberOfFiles;
			} else {
				numberOfFiles = generateRandomNumber(minNumberOfFiles, maxNumberOfFiles);
			}

			log.debug("generating {} files", numberOfFiles);

			generateManyFilesInParentCollectionByAbsolutePath(localFile.getAbsolutePath() + '/', filePrefix, fileSuffix,
					numberOfFiles, fileLengthMin, fileLengthMax);
			generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(localFile.getAbsolutePath() + "/",
					collectionPrefix, numberOfCollectionsMin, numberOfCollectionsMax, depth - 1, filePrefix, fileSuffix,
					maxNumberOfFiles, minNumberOfFiles, fileLengthMin, fileLengthMax);
		}

	}

	public static void generateManyFilesInParentCollectionByAbsolutePath(final String absolutePathToLocalCollection,
			final String filePrefix, final String fileSuffix, final int numberOfFiles, final int fileLengthMin,
			final int fileLengthMax) throws TestConfigurationException {

		String genFileName = "";
		for (int i = 0; i < numberOfFiles; i++) {
			genFileName = filePrefix + i + fileSuffix;
			FileGenerator.generateFileOfFixedLengthGivenName(absolutePathToLocalCollection, genFileName,
					FileGenerator.generateRandomNumber(fileLengthMin, fileLengthMax));
		}

	}

	public static List<String> generateManyFilesInGivenDirectory(final String relativePathUnderScratch,
			final String filePrefix, final String fileSuffix, final int numberOfFiles, final int fileLengthMin,
			final int fileLengthMax) throws TestConfigurationException {
		// n number of random files in the source directory, with a random
		// length between the min and max

		ScratchFileUtils scratchFileUtils = new ScratchFileUtils(testingProperties);
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(relativePathUnderScratch);
		ArrayList<String> sourceFileNames = new ArrayList<String>();
		String genFileName = "";
		for (int i = 0; i < numberOfFiles; i++) {
			genFileName = filePrefix + i + fileSuffix;
			genFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, genFileName,
					FileGenerator.generateRandomNumber(fileLengthMin, fileLengthMax));
			sourceFileNames.add(genFileName);
		}

		return sourceFileNames;
	}

}
