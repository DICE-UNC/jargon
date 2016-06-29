Jargon connects to iRODS via a TCP/IP socket, typically to port 1247.   This main communications channel is used to send 
and recieve iRODS packing instructions (here are some example packing instructions) per the iRODS client protocol.  
Jargon uses the XML version of the iRODS protocol.  

In order to connect to iRODS, the following components are used, and are represented in the org.irods.jargon.core.connection.* package.

**IRODSSession**- This is a central object that manages the connection to iRODS.  A client will, under the covers, ask 
IRODSSession to return a an IRODSCommands object.  Jargon maintains a cache of connections in a ThreadLocal.  This means
 that multi-threaded applications typically never share a connection between threads.  The connection handling that IRODSSession does 
 should not typically be a concern of API users, except in special circumstances.  The IRODSSession is an expensive object 
 that also holds other information, such as the current properties that control Jargon behavior, a cache of extensible 
 metadata mappings, and other objects that should only be created once in an applciation.

**IRODSProtocolManager**- This is an interface that defines an object that can give a connected IRODSCommands object 
to the IRODSSession upon request.  The IRODSSession then returns the IRODSCommands object when done, and it is the
 responsibility of the IRODSProtocolManager to dispose of or passivate a connection when done.  The interface can be 
 implemented to just return a new connection every time (the default), or it can be extended to create proxy connections, 
 a connection pool or cache, or other special behaviors.

**IRODSAccount**- This class defines a principal who is attempting to connect to a given iRODS host and zone.
 
The IRODSSession and IRODSProtocolManager are defined such that they may be wired together using Spring or another 
inversion of control container.  There should only be one instance of each in an application, created at startup, and 
torn down at application shut-down.  These objects should not be created for every request in a servlet application, for example.

The following XML snippet shows a Spring configuration that wires together an IRODSSession and IRODSProtocol Manager, and 
uses these objects to initialize an IRODSAccessObject factory.  This factory object will be covered later in the documentation:

 
`<beans:bean id="irodsConnectionManager"`

  `class="org.irods.jargon.core.connection.IRODSSimpleProtocolManager"`

  `factory-method="instance" init-method="initialize" destroy-method="destroy">`

 `</beans:bean>`




 `<beans:bean id="irodsSession"`

  `class="org.irods.jargon.core.connection.IRODSSession" factory-method="instance">`

  `<beans:constructor-arg`

   `type="org.irods.jargon.core.connection.IRODSProtocolManager" ref="irodsConnectionManager" />`

 `</beans:bean>`

 `<beans:bean id="irodsAccessObjectFactory"`

  `class="org.irods.jargon.core.pub.IRODSAccessObjectFactoryImpl">`

  `<beans:constructor-arg ref="irodsSession"></beans:constructor-arg>`

 `</beans:bean>`
 

In the above configuration, the IRODSSimpleProtocolManager is an implementation of the IRODSProtocolManager interface 
that will create a new connection on demand, and then close that connection when returned.  The IRODSAccessObject factory 
above is used to create various objects that interact with iRODS.  Note that connections are never requested by the client, 
rather, they will be managed automatically by the IRODSAccessObjectFactory when Access Objects are created.

### IRODSFileSystem

The various objects that manage connections are encapsulated in a simple short-cut object, the IRODSFileSystem.  Creating 
an instancer of IRODSFileSystem will automatically create all of these objects, and provides convenient methods to get 
the various factories that produce Access Objects and Java i/o implementation classes that access iRODS services and data.  
The IRODSFileSystem also has methods to shut down connections, access information about the configuration of the client
 and iRODS server environment, and other helpful methods.  Clients can typically create an IRODSFileSystem as a single, 
 shared instance for an application, and then use that shared reference to do all Jargon operations.

NOTE: the IRODSFileSystem should be created once in an application, and a reference to the IRODSFileSystem object should 
be shared across all objects and threads in an application.   In the iDrop Swing GUI, the IRODSFileSystem is created once, 
and stored in a common object for all forms and GUI components.

Once an IRODSFileSystem object is created, methods are used to obtain connected service objects.  Objects and Java 
i/o objects are obtained from the IRODSFileSystem by specifying the IRODSAccount to which the service is connected.  
An IRODSFileSystem can create Access Objects and i/o objects to multiple grids simultaneously, and these objects will 
automatically create a connection per thread, avoiding issues with multiple threads using the same TCP/IP connection to 
iRODS.  There are techniques by which a connection can be shared between threads, but this is not typical or recommended usage.

The steps to connecting to iRODS and accessing services are:

**Create an IRODSFileSystem as a shared object:**

 IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();

**Create the appropriate Access Object:**

Note that an appropriate IRODSAccount object with host and user information is required to get an Access Object.  In 
this example, we're getting an access object to transfer some data via a put or get using a unit testing helper method.

` IRODSAccount irodsAccount = testingPropertiesHelper    .buildIRODSAccountFromTestProperties(testingProperties);`

`DataTransferOperations dataTransferOperations = irodsFileSystem.getIRODSAccessObjectFactory().getDataTransferOperations(irodsAccount);`
 

**Use a method on an Access Object:**

In this case, we are doing a file 'put' operation.  Note that no connection code, or protocol handling code, is required.  
In this example, the put operation specifies the source and target files as java.io.File objects.  This shows the creation 
of an IRODSFile object using the IRODSFileFactory.

`// get a java.io.File object for the local (source) file`

 `File localFile = new File(localFileName);`

  `// get a java.io.File iRODS implementation for the iRODS target file using the IRODSFileFactory`

  `IRODSFileFactory irodsFileFactory = irodsFileSystem    .getIRODSFileFactory(irodsAccount);`

  `IRODSFile destFile = irodsFileFactory    .instanceIRODSFile("/some/irods/path.txt");`

  `dataTransferOperationsAO.putOperation(localFile, destFile, null, null);`

Note that the put operation has extra parameters that can do things like control details of the transfer, and specify a 
listener to receive status callbacks and file transfer progress.  In this example, they are ignored and set to null.

**Close the connections**

Note that there are various close methods to close all connections, or connections to a specific grid.  In this case we 
are closing any connections open in this Thread.  The connection is returned to the IRODSProtocolManager, which will do 
the actual close, or could return it to a pool, etc.

irodsFileSystem.close()
 
