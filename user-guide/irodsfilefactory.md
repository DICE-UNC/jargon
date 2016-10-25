## The IRODSFileFactory

Jargon implements a set of services that map to the core java.io library for files and streams.  These objects talk though Jargon directly to the iRODS server.  In order to create the various file and stream objects, a 
factory object is provided in [IRODSFileFactory](https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/main/java/org/irods/jargon/core/pub/io/IRODSFileFactory.java).

The IRODSFile factory creates the java.io implementation classes and provides them with internal connections to iRODS (that's
why it's done that way).  Simply, create an instance of the factory, then use it to build the .io class you need.  Here
is a sample of some of the factory methods:

```

/**
	 * Creates an iRODS version of an input stream such that data can be read
	 * from the source iRODS file.
	 *
	 * @param irodsFile
	 *            {@link IRODSFile} that will be the source of the stream
	 * @return {@link IRODSFileInputStream} that allows reading of the contents
	 *         of the iRODS file
	 * @throws JargonException
	 */
	IRODSFileInputStream instanceIRODSFileInputStream(IRODSFile irodsFile)
			throws JargonException;

	/**
	 * Creates an iRODS input stream such that data can be read to the given
	 * iRODS file.
	 *
	 * @param name
	 *            <code>String</code> with and absolute path to the file that
	 *            will be read to via the given stream.
	 * @return {@link IRODSFileInputStream} implementation of a
	 *         <code>java.io.InputStream</code>
	 * @throws JargonException
	 */
	IRODSFileInputStream instanceIRODSFileInputStream(String name)
			throws JargonException;

	/**
	 * Creates an iRODS input stream such that data can be read to the given
	 * iRODS file.
	 * <p/>
	 * Note that this method signature will do any necessary connection
	 * re-routing based to a resource actually containing the file. If such
	 * rerouting is done, the <code>InputStream</code> will be wrapped with a
	 * {@link SessionClosingIRODSFileInputStream} that will close the re-routed
	 * connection when the stream is closed.
	 *
	 * @param name
	 *            <code>String</code> with and absolute path to the file that
	 *            will be read to via the given stream.
	 * @return {@link IRODSFileInputStream} implementation of a
	 *         <code>java.io.InputStream</code>
	 * @throws JargonException
	 */
	IRODSFileInputStream instanceIRODSFileInputStreamWithRerouting(
			String irodsAbsolutePath) throws JargonException;



```

Note that since connections are created, it is important to close connections afterwords.  The IRODSAccessObjectFactory,
and IRODSFileSystem both provide close methods.  This unit test shows an i/o object being created and used:

```

@Test
	public final void testCreateOutStreamFromFileName() throws Exception {
		String testFileName = "testCreateOutStreamFromFileName.csv";

		String absPath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName,
				1);

		String targetIrodsCollection = testingPropertiesHelper
				.buildIRODSCollectionAbsolutePathFromTestProperties(
						testingProperties, IRODS_TEST_SUBDIR_PATH);

		StringBuilder fileNameAndPath = new StringBuilder();
		fileNameAndPath.append(absPath);

		fileNameAndPath.append(testFileName);

		IRODSAccount irodsAccount = testingPropertiesHelper
				.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSAccessObjectFactory accessObjectFactory = irodsFileSystem
				.getIRODSAccessObjectFactory();

		DataTransferOperations dto = accessObjectFactory
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(
				fileNameAndPath.toString(),
				targetIrodsCollection,
				testingProperties
				.getProperty(TestingPropertiesHelper.IRODS_RESOURCE_KEY),
				null, null);

		IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFileOutputStream fos = irodsFileFactory
				.instanceIRODSFileOutputStream(targetIrodsCollection + '/'
						+ testFileName);
		Assert.assertNotNull("null output stream returned from initializer",
				fos);

	}

```