## Reading data from iRODS using Jargon

There are two ways to read iRODS data locally.  The first way is to move the entire file from iRODS to the client machine.
This is analogous to the [iget iCommand](https://docs.irods.org/master/icommands/user/#iget).  This is a transfer of the file from iRODS to the local
file system.  The second way is to read the file as a stream, using an extension of the [java.io.InputStream](https://docs.oracle.com/javase/7/docs/api/java/io/InputStream.html)
Which allows read, write, skip, and other expected operations.  

Generally, a transfer is much faster than streaming i/o, especially since the transfer method can use parallel i/o or
future pluggable transport mechanisms.  Streaming i/o is indicated when a subset of a file is to be read, or if the purpose requires
a streaming mode, such as to support REST or HTTP transfers.

### Getting data via iRODS transfers

The [DataTransferOperations](https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/main/java/org/irods/jargon/core/pub/DataTransferOperations.java) service 
object provides methods to 'get' data from iRODS. Specifically, the getOperation()

```
void getOperation(String irodsSourceFileAbsolutePath,
			String targetLocalFileAbsolutePath, String sourceResourceName,
			TransferStatusCallbackListener transferStatusCallbackListener,
			TransferControlBlock transferControlBlock)
throws FileNotFoundException, OverwriteException, JargonException;

```

The unit test code for transfers has several examples of a get operation with various modes and parameters.  In the above 
operation, the absolute path for the local file is the target of the transfer, and it will transfer the contents from the iRODS
absolute path to the file at the local path.  There are two additional parameters to this method.  First is the [TransferStatusCallbackListener](https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/main/java/org/irods/jargon/core/transfer/TransferStatusCallbackListener.java).
The TransferStatusCallbackListener can be any class that implements the described interface. 

 
### Callback listeners
 
The [TransferStatusCallbackListener](https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/main/java/org/irods/jargon/core/transfer/TransferStatusCallbackListener.java). 
interface provides callback hooks that will receive notifications as a transfer process proceeds.  This can be used to drive progress 
bars and messages, or for any other purpose.  The transfer callbacks follow a defined pattern:

* An overall callback is provided at the start of an entire transfer
* A callback at the start of file (there may be many if a collection is being transferred)
* If specified through jargon.properties, or in the TransferControlBlock, an 'intra-file' callback that periodically reports
n bytes of n total for a file
* A callback at the end of a file
* A callback at the end of the entire transfer

One may implement this interface, and add it as a parameter to receive these callbacks.  Notably, this is the mechanism used
to notify the client of a potential overwrite situation.  If a file transfer would overlay an existing file, then the transfer
 will make a callback at 
 
 ```
 
 public FileStatusCallbackResponse statusCallback(
 final TransferStatus transferStatus) throws JargonException;
 
 ```
 
 And the client can then return a response
 
 ```
 public enum CallbackResponse {
 		YES_THIS_FILE, NO_THIS_FILE, YES_FOR_ALL, NO_FOR_ALL, CANCEL
 }
 
 ```
 
 That indicates how this overwrite situation should be handled.
 
### The TransferControlBlock
 
A transfer may be running in a separate thread from a GUI event thread, or for some other reason the transferring process my be 
 in a separate thread from the main control thread.  The [TransferControlBlock](https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/main/java/org/irods/jargon/core/transfer/TransferControlBlock.java)
 provides a hook so that a client can interact with the running transfer.  This includes sending signals to pause or 
 cancel a transfer, or to get live statistics about a transfer (number of files, bytes, errors, etc).  One may also set a 'last successful'
 file path, and thus cause a restart to occur, skipping any files before that restart point.  
 
 Transfers may be controlled through the manipulation of many parameters.  the jargon.properties file contains many options
 that may be set globally.  During a jargon invocation, there is a system to manage these global parameters, and the [TransferOptions](https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/main/java/org/irods/jargon/core/packinstr/TransferOptions.java)
 of the TransferControlBlock allows override of specific properties such as max threads for parallel transfer, whether to get intra-file callbacks, checksum options, etc.  If you want
 to customize the behavior of a particular transfer, this is the place to set them.
 
 ### An example of a get operation
 
 The unit tests in Jargon highlight many operations.  Here is an example of a standard get that takes default settings and does not use a callback listener.  This particular test sets a property so
 that resource re-routing occurs.  This will switch the connection to the location in the iRODS grid where the data actually resides,
 which can have positive performance benefits.
 
 ```
 @Test
 	public void testGetOneFileWithResourceRerouting() throws Exception {
 
 		if (!testingPropertiesHelper
 				.isTestDistributedResources(testingProperties)) {
 			return;
 		}
 
 		String testFileName = "testGetOneFileWithResourceRerouting.txt";
 		String testRetrievedFileName = "testGetOneFileWithResourceReroutingRetrieved.txt";
 
 		String absPath = scratchFileUtils
 				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
 		String localFileName = FileGenerator
 				.generateFileOfFixedLengthGivenName(absPath, testFileName, 300);
 
 		String targetIrodsFile = testingPropertiesHelper
 				.buildIRODSCollectionAbsolutePathFromTestProperties(
 						testingProperties, IRODS_TEST_SUBDIR_PATH + '/'
 								+ testFileName);
 		File localFile = new File(localFileName);
 
 		// now put the file
 
 		IRODSAccount irodsAccount = testingPropertiesHelper
 				.buildIRODSAccountFromTestProperties(testingProperties);
 
 		irodsFileSystem.closeAndEatExceptions();
 
 		IRODSFileFactory irodsFileFactory = irodsFileSystem
 				.getIRODSFileFactory(irodsAccount);
 		IRODSFile destFile = irodsFileFactory
 				.instanceIRODSFile(targetIrodsFile);
 		destFile.setResource(testingProperties
 				.getProperty(TestingPropertiesHelper.IRODS_TERTIARY_RESOURCE_KEY));
 		DataTransferOperations dataTransferOperationsAO = irodsFileSystem
 				.getIRODSAccessObjectFactory().getDataTransferOperations(
 						irodsAccount);
 
 		SettableJargonProperties settableProperties = new SettableJargonProperties();
 		settableProperties.setAllowPutGetResourceRedirects(true);
 		irodsFileSystem.getIrodsSession().setJargonProperties(
 				settableProperties);
 
 		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);
 
 		File retrieveFile = new File(absPath + "/" + testRetrievedFileName);
 		dataTransferOperationsAO.getOperation(destFile, retrieveFile, null,
 				null);
 		Assert.assertTrue("retrieved file should exist", retrieveFile.exists());
 
 		irodsFileSystem.closeAndEatExceptions(irodsAccount);
 		// there should only be one connection in the session map (secondary
 		// account should have been closed
 		Assert.assertNull("session from reroute leaking",
 				irodsFileSystem.getConnectionMap());
 
 	}
 
 ```
 
Note that, controlled by the jargon.properties and transfer options, the transfer will be accomplished in the most efficient
way possible, including setting buffering options, using parallel where required, and other modalities of transfer. It should
be mostly 'fire and forget'.

### Streaming i/o reading of files

Java provides an i/o library that defines standard input streams and random file operations.  Jargon provides an implementation
of Java i/o that communicates with an iRODS grid under the covers.  These implementations are in the Jargon [core.pub.io](https://github.com/DICE-UNC/jargon/tree/master/jargon-core/src/main/java/org/irods/jargon/core/pub/io)
package.  In this package are several key classes and interfaces that allow reading of data from iRODS.  The various i/o packages are created using the IRODSFileFactory which is described [here](irodsfilefactory.md).

The basic facility to stream files from iRODS is in the Jargon version of the standard java.io.FileInputStream.  This is the [IRODSFileInputStream](https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/main/java/org/irods/jargon/core/pub/io/IRODSFileInputStream.java)
Once created by the IRODSFileFactory, this can be used in the same fashion as the standard java.io.FileInputStream.  This example is from the FileInputStream [unit test](https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/test/java/org/irods/jargon/core/pub/io/IRODSFileInputStreamTest.java)

```
IRODSFileFactory irodsFileFactory = accessObjectFactory
				.getIRODSFileFactory(irodsAccount);
		IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(
				targetIrodsCollection, testFileName);
		IRODSFileInputStream fis = irodsFileFactory
				.instanceIRODSFileInputStream(irodsFile);

		ByteArrayOutputStream actualFileContents = new ByteArrayOutputStream();

		int bytesRead = 0;
		int readLength = 0;
		byte[] readBytesBuffer = new byte[1024];
		while ((readLength = (fis.read(readBytesBuffer))) > -1) {
			actualFileContents.write(readBytesBuffer);
			bytesRead += readLength;
		}


```

All of the standard i/o contracts are honored by the i/o libraries, and standard buffering techniques and stream wrapping operations 
are supported (i.e. wrapping a stream with a buffer or reader).  Use them just like the i/o libraries.  There are also
several variants on the basic stream in the [core.io](https://github.com/DICE-UNC/jargon/tree/master/jargon-core/src/main/java/org/irods/jargon/core/pub/io).  

Here are a few highlights of that i/o library worth noting:

* [IRODSFileReader](https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/main/java/org/irods/jargon/core/pub/io/IRODSFileReader.java) reads and does character 
encoding 
* [IRODSRandomAccessFile](https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/main/java/org/irods/jargon/core/pub/io/IRODSRandomAccessFile.java) for random i/o 
operations
* [PackingIrodsInputStream](https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/main/java/org/irods/jargon/core/pub/io/PackingIrodsInputStream.java) is an enhanced 
IRODSFileInputStream that does read-ahead and write-behind buffering, and is highly recommended.  It's used in REST and WebDav and the Cloud Browser!  See the comment below
on stream i/o performance

### Stream i/o performance

Standard 'put/get' transfer operations typically outperform streaming operations.  This is for many reasons, but in a nutshell the protocol for a transfer is 
one message that says "here comes the data", and then the raw bytes are shoved down the pipe.  For streaming i/o, each individual read of a buffer is 
a complete protocol request, and this ends up being much slower.  This is especially true when the buffer being 
used for the read operation is small.  If, for example, a program attempts to read from a stream in 8K increments,
the protocol overhead is amortized over a smaller amount of data'.  The PackingIrodsInputStream and output stream allow a
program to read and write in smaller buffer sizes, but under the covers accumulate a much larger byte buffer before
making a call to iRODS, this amortizes the protocol overhead over a much bigger payload.































