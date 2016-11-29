## Writing data to iRODS using Jargon

There are two ways to write iRODS data locally.  The first way is to move the entire file to iRODS from the client machine.
This is analogous to the [iput iCommand](https://docs.irods.org/master/icommands/user/#iput).  This is a transfer of the file to iRODS from the local
file system.  The second way is to write the file using streaming or random access file i/o.

Generally, a transfer is much faster than streaming i/o, especially since the transfer method can use parallel i/o or
future pluggable transport mechanisms.  Streaming i/o is indicated when a subset of a file is to be writter, or if the purpose requires
a streaming mode, such as to support REST or HTTP transfers, like HTTP uploads from a browser.


### Sending data to iRODS  using a 'put' transfer

The [DataTransferOperations](https://github.com/DICE-UNC/jargon/blob/master/jargon-core/src/main/java/org/irods/jargon/core/pub/DataTransferOperations.java) service 
object provides methods to 'put' data from iRODS. Specifically, the putOperation()

```
/**
	 * Put a file or a collection (recursively) to iRODS. This method allows
	 * registration of a <code>TransferStatusCallbackListener</code> that will
	 * provide call-backs about the status of the transfer suitable for progress
	 * monitoring.
	 * <p/>
	 * Note that this method will, if the correct jargon properties are set,
	 * support connection re-routing to appropriate resources for the transfer.
	 * <p/>
	 * Note: certain extended transfer options for put (such as target data
	 * type) are set in the {@link TransferOptions} object, which is specified
	 * by creating and passing within the {@link TransferControlBlock}. For
	 * example, setting a data object to be an MSSO object is accomplished by
	 * setting that type in the {@link TransferOptions} object.
	 *
	 * @param sourceFile
	 *            <code>File</code> with the source directory or file.
	 * @param targetIrodsFile
	 *            {@link org.irods.jargon.core.pub.io.IRODSFile} with the target
	 *            iRODS file or collection.
	 * @param transferControlBlock
	 *            an optional
	 *            {@link org.irods.jargon.core.transfer.TransferControlBlock}
	 *            that provides a common object to communicate between the
	 *            object requesting the transfer, and the method performing the
	 *            transfer. This control block may contain a filter that can be
	 *            used to control restarts, and provides a way for the
	 *            requesting process to send a cancellation. This may be set to
	 *            null if not required.
	 * @throws OverwriteException
	 *             if an overwrite is attempted and the force option has not
	 *             been set
	 * @throws DataNotFoundException
	 *             if the source iRODS file does not exist
	 * @throws JargonException
	 */
	void putOperation(
			final File sourceFile,
			final IRODSFile targetIrodsFile,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final TransferControlBlock transferControlBlock)
					throws DataNotFoundException, OverwriteException, JargonException;

```


