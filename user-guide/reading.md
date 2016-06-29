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



































