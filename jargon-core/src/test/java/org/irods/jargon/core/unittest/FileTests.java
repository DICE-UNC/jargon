package org.irods.jargon.core.unittest;

import org.irods.jargon.core.pub.io.FileIOOperationsAOImplTest;
import org.irods.jargon.core.pub.io.IRODSFIleInputStreamTest;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImplTest;
import org.irods.jargon.core.pub.io.IRODSFileImplTest;
import org.irods.jargon.core.pub.io.IRODSFileOutputStreamTest;
import org.irods.jargon.core.pub.io.IRODSFileReaderTest;
import org.irods.jargon.core.pub.io.IRODSFileSystemAOImplTest;
import org.irods.jargon.core.pub.io.IRODSFileWriterTest;
import org.irods.jargon.core.pub.io.IRODSRandomAccessFileTest;
import org.irods.jargon.core.pub.io.RemoteExecutionBinaryResultInputStreamTest;
import org.irods.jargon.core.pub.io.SessionClosingIRODSFIleInputStreamTest;
import org.irods.jargon.core.pub.io.SessionClosingIRODSFileOutputStream;
import org.irods.jargon.core.pub.io.SessionClosingIRODSFileOutputStreamTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ IRODSFileFactoryImplTest.class, IRODSFileImplTest.class,
		IRODSFileSystemAOImplTest.class, IRODSFileOutputStreamTest.class,
		IRODSFIleInputStreamTest.class, FileIOOperationsAOImplTest.class,
		IRODSRandomAccessFileTest.class, IRODSFileWriterTest.class,
		IRODSFileReaderTest.class,
		SessionClosingIRODSFIleInputStreamTest.class, SessionClosingIRODSFileOutputStreamTest.class,
		RemoteExecutionBinaryResultInputStreamTest.class })
public class FileTests {

}
