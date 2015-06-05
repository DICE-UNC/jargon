package org.irods.jargon.core.unittest;

import org.irods.jargon.core.pub.io.ByteCountingCallbackInputStreamWrapperTest;
import org.irods.jargon.core.pub.io.FederatedIRODSFileImplTest;
import org.irods.jargon.core.pub.io.FileIOOperationsAOImplTest;
import org.irods.jargon.core.pub.io.IRODSFIleInputStreamForSoftLinksTest;
import org.irods.jargon.core.pub.io.IRODSFileFactoryImplTest;
import org.irods.jargon.core.pub.io.IRODSFileImplForSoftLinksTest;
import org.irods.jargon.core.pub.io.IRODSFileImplTest;
import org.irods.jargon.core.pub.io.IRODSFileInputStreamTest;
import org.irods.jargon.core.pub.io.IRODSFileOutputStreamForSoftLinksTest;
import org.irods.jargon.core.pub.io.IRODSFileOutputStreamTest;
import org.irods.jargon.core.pub.io.IRODSFileReaderTest;
import org.irods.jargon.core.pub.io.IRODSFileSystemAOImplTest;
import org.irods.jargon.core.pub.io.IRODSFileWriterTest;
import org.irods.jargon.core.pub.io.IRODSRandomAccessFileTest;
import org.irods.jargon.core.pub.io.MountedFileSystemIRODSFileInputStreamTest;
import org.irods.jargon.core.pub.io.MountedFilesystemIRODSFileImplTest;
import org.irods.jargon.core.pub.io.MountedFilesystemIRODSFileOutputStreamTest;
import org.irods.jargon.core.pub.io.PackingIrodsOutputStreamTest;
import org.irods.jargon.core.pub.io.RemoteExecutionBinaryResultInputStreamTest;
import org.irods.jargon.core.pub.io.SessionClosingIRODSFIleInputStreamTest;
import org.irods.jargon.core.pub.io.SessionClosingIRODSFileOutputStreamTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ IRODSFileFactoryImplTest.class, IRODSFileImplTest.class,
		IRODSFileSystemAOImplTest.class, IRODSFileOutputStreamTest.class,
		IRODSFileInputStreamTest.class, FileIOOperationsAOImplTest.class,
		IRODSRandomAccessFileTest.class, IRODSFileWriterTest.class,
		IRODSFileReaderTest.class,
		SessionClosingIRODSFIleInputStreamTest.class,
		SessionClosingIRODSFileOutputStreamTest.class,
		RemoteExecutionBinaryResultInputStreamTest.class,
		ByteCountingCallbackInputStreamWrapperTest.class,
		FederatedIRODSFileImplTest.class, IRODSFileImplForSoftLinksTest.class,
		IRODSFIleInputStreamForSoftLinksTest.class,
		IRODSFileOutputStreamForSoftLinksTest.class,
		MountedFilesystemIRODSFileImplTest.class,
		MountedFileSystemIRODSFileInputStreamTest.class,
		MountedFilesystemIRODSFileOutputStreamTest.class,
		PackingIrodsOutputStreamTest.class })
public class FileTests {

}
