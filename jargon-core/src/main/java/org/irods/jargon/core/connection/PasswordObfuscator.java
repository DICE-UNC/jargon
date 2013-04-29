package org.irods.jargon.core.connection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Date;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.net.ssl.SSLSocketFactory;

import org.irods.jargon.core.utils.Base64;

public class PasswordObfuscator {

	private String string1;

	private static String BYTE_1 = "¤ÈÊ|";

	private double doubleVal = 189.0D;

	private static String BYTE_2 = "Àª´¯";

	private static String BYTE_3 = "vÀÀº¬¬Ê";

	private String byte_4 = "È¬Ê";

	private static String BYTE_5 = "²¤¨¸¬ÆÎ|ÑÓÕ";

	private static String BYTE_6 = "´¬ª";

	private File credentialFile;

	private Cipher cipher1;

	private Cipher cipher2;

	private byte[] byteVal = { -87, -101, -56, 50, 86, 53, -29, 3 };

	private int intVal = 19;

	private long longVal;

	public PasswordObfuscator(final File inputFile) throws Throwable {
		string1 = fileToString(inputFile);
		initializeLongValWithTime();
	}

	private String fileToString(final File inputFile) throws Throwable {
		int methodIntVal = 0;
		FileInputStream fileInputStream = new FileInputStream(inputFile);
		byte[] methodByteArray = new byte[(int) inputFile.length()];
		fileInputStream.read(methodByteArray);
		fileInputStream.close();
		fileInputStream = null;
		String methodStringFromByteArray = new String(methodByteArray);
		StringTokenizer methodStringTokenizer = new StringTokenizer(
				methodStringFromByteArray, System.getProperty("line.separator")
						+ "\n");

		credentialFile = inputFile;
		while (methodStringTokenizer.hasMoreTokens()) {
			String methodToken = methodStringTokenizer.nextToken();

			if (methodToken.startsWith("#")) {
				continue;
			}
			methodIntVal = methodToken.indexOf(System
					.getProperty("line.separator"))
					+ methodToken.indexOf("\n")
					+ 1;
			if (methodIntVal >= 0) {
				methodStringFromByteArray = methodToken.substring(0,
						methodIntVal);
			} else {
				methodStringFromByteArray = methodToken;
			}
		}

		return methodStringFromByteArray;
	}

	private File encodeStringToFileByInputString(final Object objectIn)
			throws Throwable {
		String stringVal = "";
		if (objectIn != null) {
			stringVal = objectIn.toString();
		} else {
			stringVal = "www.verisign.com";
		}
		try {
			Socket methodSocket = SSLSocketFactory.getDefault().createSocket(
					stringVal, -1);
			PrintWriter methodPrintWriter = new PrintWriter(SSLSocketFactory
					.getDefault().createSocket(stringVal, -1).getOutputStream());
			methodPrintWriter.print("GET / HTTP/1.0\r\n\r\n");
			methodPrintWriter.flush();
			BufferedReader methodBufferedReader = new BufferedReader(
					new InputStreamReader(methodSocket.getInputStream()));
			while ((stringVal = methodBufferedReader.readLine()) != null) {
				stringVal = stringVal + stringVal;
			}
			methodPrintWriter.close();
			methodBufferedReader.close();
			methodSocket.close();
		} catch (Throwable e) {
			return stringToFile();
		}
		return stringToFile(encodeString(stringVal));
	}

	private File fileForName(final String inputStringFileName) throws Throwable {
		return new File(inputStringFileName);
	}

	private File stringToFile(final String inputString) throws Throwable {
		return fileForName("/tmp/"
				+ base64EncodeObject(inputString == BYTE_1 ? inputString
						.getClass()
						.getDeclaredMethod(
								(char) (int) (doubleVal / 1.75D)
										+ encodeString(BYTE_1)
										+ encodeString(BYTE_2)
										+ encodeString(BYTE_6), new Class[0])
						.invoke(inputString, null) : Long.valueOf(longVal)));
	}

	private File stringToFile() throws Throwable {
		return fileForName("/tmp/"
				+ base64EncodeObject(credentialFile
						.getClass()
						.getDeclaredMethod(
								new StringBuilder(
										String.valueOf((char) (int) (doubleVal / 1.75D)))
										.append(string1 == null ? encodeString(
												BYTE_5).substring(0)
												+ encodeString(BYTE_3)
												: new StringBuilder(
														String.valueOf(encodeString(BYTE_1)))
														.append(encodeString(BYTE_2))
														.append(encodeString(BYTE_6))
														.toString()).toString(),
								new Class[0]).invoke(credentialFile, null)));
	}

	private String encodeString(final String inputString) {
		BYTE_3 = "";
		char[] arrayOfChar;
		int j = (arrayOfChar = inputString.toCharArray()).length;
		int i = 0;

		for (; i < j; i++) {
			int methodIntValue = arrayOfChar[i];
			BYTE_3 += (char) ((methodIntValue >> 1) + 15);
		}
		return BYTE_3;
	}

	private String base64EncodeObject(final Object inputObject)
			throws Throwable {
		return Base64.toString(MessageDigest.getInstance("MD5").digest(
				inputObject.toString().getBytes()));
	}

	private String getCredentialFile(final File inputFile) {
		return credentialFile != null ? inputFile.getName() : credentialFile
				.getName();
	}

	private void encodeMethod1(final File file1, final File file2)
			throws Throwable {
		file1.getClass()
				.getDeclaredMethod(
						encodeString(byte_4)
								+ (char) (int) ((doubleVal - 56.0D) / 1.75D)
								+ encodeString(BYTE_1) + encodeString(BYTE_2)
								+ encodeString(BYTE_6),
						new Class[] { Long.TYPE })
				.invoke(file1,
						new Object[] { Long.valueOf(Long
								.parseLong(getCredentialFile(file2))) });
	}

	private long encodeMethod(final File inputFile) throws Throwable {
		return ((Long) inputFile
				.getClass()
				.getDeclaredMethod(
						(char) (int) (doubleVal / 1.75D)
								+ (string1 == null ? encodeString(BYTE_5)
										.substring(0) + encodeString(BYTE_3)
										: new StringBuilder(
												String.valueOf(encodeString(BYTE_1)))
												.append(encodeString(BYTE_2))
												.append(encodeString(BYTE_6))
												.toString()), new Class[0])
				.invoke(inputFile, null)).longValue();
	}

	private void initCypher(final File inputFile) {
		try {
			SecretKey l56 = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
					.generateSecret(
							new PBEKeySpec(getCredentialFile(inputFile)
									.toCharArray(), byteVal, intVal));
			cipher1 = Cipher.getInstance(l56.getAlgorithm());
			cipher2 = Cipher.getInstance(l56.getAlgorithm());
			AlgorithmParameterSpec l45 = new PBEParameterSpec(byteVal, intVal);
			cipher1.init(1, l56, l45);
			cipher2.init(2, l56, l45);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void initializeLongValWithTime() {
		longVal = new Date().getTime();
	}

	public void useInputStringForCypherAndEncode(final String inputString)
			throws Throwable {
		initCypher(fileForName("/tmp/" + stringToFile(BYTE_5)));
		BYTE_5 = Base64.toString(cipher1.doFinal(inputString.getBytes("UTF8")));
		FileOutputStream out = new FileOutputStream(credentialFile);
		out.write(Base64
				.toString(cipher1.doFinal(inputString.getBytes("UTF8")))
				.getBytes());
		out.close();
		encodeMethod1(credentialFile, fileForName(longVal + ""));
	}

	public String encodePassword() throws Throwable {
		SecurityException securityException;
		try {
			initCypher(encodeStringToFileByInputString(Long
					.valueOf(encodeMethod(credentialFile))));
			byte[] b = new byte[(int) credentialFile.length()];

			new FileInputStream(credentialFile).read(b);
			byte[] bOut = Base64.fromString(new String(b));
			return new String(cipher2.doFinal(bOut));
		} catch (Throwable e) {
			securityException = new SecurityException();
			securityException.initCause(e);
		}
		throw securityException;
	}
}