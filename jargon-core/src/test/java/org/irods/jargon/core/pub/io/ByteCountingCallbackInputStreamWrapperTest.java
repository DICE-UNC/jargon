package org.irods.jargon.core.pub.io;

import java.io.InputStream;

import org.irods.jargon.core.connection.ConnectionProgressStatus;
import org.irods.jargon.core.connection.ConnectionProgressStatusListener;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import junit.framework.Assert;

public class ByteCountingCallbackInputStreamWrapperTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testReadByteArray() throws Exception {
		int expectedLen = 100;
		InputStream dummyInputStream = Mockito.mock(InputStream.class);
		ConnectionProgressStatusListener listener = Mockito.mock(ConnectionProgressStatusListener.class);
		@SuppressWarnings("resource")
		ByteCountingCallbackInputStreamWrapper byteCountingCallbackInputStream = new ByteCountingCallbackInputStreamWrapper(
				listener, dummyInputStream);
		byte[] readBuffer = new byte[100];
		Mockito.when(dummyInputStream.read(readBuffer)).thenReturn(expectedLen);

		int actualRead = byteCountingCallbackInputStream.read(readBuffer);
		Assert.assertEquals("did not get expected length back", expectedLen, actualRead);
		Mockito.verify(listener).connectionProgressStatusCallback(Matchers.isA(ConnectionProgressStatus.class));
	}

	@Test
	public void testReadByteAvailable() throws Exception {
		int expectedLen = 100;
		InputStream dummyInputStream = Mockito.mock(InputStream.class);
		ConnectionProgressStatusListener listener = Mockito.mock(ConnectionProgressStatusListener.class);
		@SuppressWarnings("resource")
		ByteCountingCallbackInputStreamWrapper byteCountingCallbackInputStream = new ByteCountingCallbackInputStreamWrapper(
				listener, dummyInputStream);
		Mockito.when(dummyInputStream.available()).thenReturn(expectedLen);

		int actualRead = byteCountingCallbackInputStream.available();
		Assert.assertEquals("did not get expected available back", expectedLen, actualRead);

	}

	@Test
	public void testClose() throws Exception {
		InputStream dummyInputStream = Mockito.mock(InputStream.class);
		ConnectionProgressStatusListener listener = Mockito.mock(ConnectionProgressStatusListener.class);
		ByteCountingCallbackInputStreamWrapper byteCountingCallbackInputStream = new ByteCountingCallbackInputStreamWrapper(
				listener, dummyInputStream);

		byteCountingCallbackInputStream.close();
		Mockito.verify(dummyInputStream).close();

	}

	@Test
	public void testReadByteArrayIntInt() throws Exception {
		int expectedLen = 100;
		InputStream dummyInputStream = Mockito.mock(InputStream.class);
		ConnectionProgressStatusListener listener = Mockito.mock(ConnectionProgressStatusListener.class);
		@SuppressWarnings("resource")
		ByteCountingCallbackInputStreamWrapper byteCountingCallbackInputStream = new ByteCountingCallbackInputStreamWrapper(
				listener, dummyInputStream);
		byte[] readBuffer = new byte[100];
		Mockito.when(dummyInputStream.read(readBuffer, 0, expectedLen)).thenReturn(expectedLen);

		int actualRead = byteCountingCallbackInputStream.read(readBuffer, 0, expectedLen);
		Assert.assertEquals("did not get expected length back", expectedLen, actualRead);
		Mockito.verify(listener).connectionProgressStatusCallback(Matchers.isA(ConnectionProgressStatus.class));
	}

	@SuppressWarnings("resource")
	@Test
	public void testByteCountingCallbackInputStreamWrapper() {
		InputStream dummyInputStream = Mockito.mock(InputStream.class);
		ConnectionProgressStatusListener listener = Mockito.mock(ConnectionProgressStatusListener.class);
		new ByteCountingCallbackInputStreamWrapper(listener, dummyInputStream);
		Assert.assertTrue(true);
	}

	@SuppressWarnings("resource")
	@Test(expected = IllegalArgumentException.class)
	public void testByteCountingCallbackInputStreamWrapperNullListener() {
		InputStream dummyInputStream = Mockito.mock(InputStream.class);
		ConnectionProgressStatusListener listener = null;
		new ByteCountingCallbackInputStreamWrapper(listener, dummyInputStream);
	}

	@SuppressWarnings("resource")
	@Test(expected = IllegalArgumentException.class)
	public void testByteCountingCallbackInputStreamWrapperNullStream() {
		InputStream dummyInputStream = null;
		ConnectionProgressStatusListener listener = Mockito.mock(ConnectionProgressStatusListener.class);
		new ByteCountingCallbackInputStreamWrapper(listener, dummyInputStream);
	}

}
