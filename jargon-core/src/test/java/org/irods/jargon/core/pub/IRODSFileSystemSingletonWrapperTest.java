package org.irods.jargon.core.pub;

import org.irods.jargon.core.exception.JargonException;
import org.junit.Assert;
import org.junit.Test;

public class IRODSFileSystemSingletonWrapperTest {

	@Test
	public void testInstance() throws JargonException {
		IRODSFileSystem irodsFileSystem = IRODSFileSystemSingletonWrapper.instance();
		Assert.assertNotNull(irodsFileSystem);
	}

}
