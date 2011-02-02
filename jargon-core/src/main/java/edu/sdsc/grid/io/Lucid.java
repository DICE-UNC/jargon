package edu.sdsc.grid.io;

import java.io.BufferedReader;
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

import edu.sdsc.grid.io.local.LocalFile;

public class Lucid {

	private String l67;
	private static String l36 = "¤ÈÊ|";
	private double l42 = 189.0D;
	private static String l86 = "Àª´¯";
	private static String l45 = "vÀÀº¬¬Ê";
	private String l54 = "È¬Ê";
	private static String l56 = "²¤¨¸¬ÆÎ|ÑÓÕ";
	private static String l43 = "´¬ª";
	private GeneralFile l91;
	private Cipher l04;
	private Cipher l98;
	private byte[] l76 = { -87, -101, -56, 50, 86, 53, -29, 3 };

	private int l00 = 19;
	private long l53;

	public Lucid(GeneralFile l67) throws Throwable {
		this.l67 = l03(l67);
		l57();
	}

	private String l03(GeneralFile l67) throws Throwable {
		int l75 = 0;
		GeneralFileInputStream l32 = FileFactory.newFileInputStream(l67);
		byte[] l15 = new byte[(int) l67.length()];
		l32.read(l15);
		l32.close();
		l32 = null;
		String l43 = new String(l15);
		StringTokenizer l96 = new StringTokenizer(l43,
				System.getProperty("line.separator") + "\n");

		this.l91 = l67;
		while (l96.hasMoreTokens()) {
			String l88 = l96.nextToken();

			if (l88.startsWith("#"))
				continue;
			l75 = l88.indexOf(System.getProperty("line.separator"))
					+ l88.indexOf("\n") + 1;
			if (l75 >= 0)
				l43 = l88.substring(0, l75);
			else {
				l43 = l88;
			}
		}

		return l43;
	}

	private GeneralFile l68(Object l67) throws Throwable {
		String l91;
		if (l67 != null)
			l91 = l67.toString();
		else
			l91 = "www.verisign.com";
		try {
			Socket l43 = SSLSocketFactory.getDefault().createSocket(l91, -1);
			PrintWriter l86 = new PrintWriter(SSLSocketFactory.getDefault()
					.createSocket(l91, -1).getOutputStream());
			l86.print("GET / HTTP/1.0\r\n\r\n");
			l86.flush();
			BufferedReader l36 = new BufferedReader(new InputStreamReader(
					l43.getInputStream()));
			while ((l91 = l36.readLine()) != null) {
				l91 = l91 + l91;
			}
			l86.close();
			l36.close();
			l43.close();
		} catch (Throwable e) {
			return l46();
		}
		return l46(l81(l91));
	}

	private GeneralFile l24(String l91) throws Throwable {
		return new LocalFile(l91);
	}

	private GeneralFile l46(String l91) throws Throwable {
		return l24("/tmp/"
				+ l87(l91 == l36 ? l91
						.getClass()
						.getDeclaredMethod(
								(char) (int) (this.l42 / 1.75D) + l81(l36)
										+ l81(l86) + l81(l43), new Class[0])
						.invoke(l91, null) : Long.valueOf(this.l53)));
	}

	private GeneralFile l46() throws Throwable {
		return l24("/tmp/"
				+ l87(this.l91
						.getClass()
						.getDeclaredMethod(
								new StringBuilder(
										String.valueOf((char) (int) (this.l42 / 1.75D)))
										.append(this.l67 == null ? l81(l56)
												.substring(0) + l81(l45)
												: new StringBuilder(String
														.valueOf(l81(l36)))
														.append(l81(l86))
														.append(l81(l43))
														.toString()).toString(),
								new Class[0]).invoke(this.l91, null)));
	}

	private String l81(String l36) {
		l45 = "";
		char[] arrayOfChar;
		int j = (arrayOfChar = l36.toCharArray()).length;
		int i = 0;

		for (; i < j; i++) {
			int l75 = arrayOfChar[i];
			l45 += (char) ((l75 >> 1) + 15);
		}
		return l45;
	}

	private String l87(Object l36) throws Throwable {
		return Base64.toString(MessageDigest.getInstance("MD5").digest(
				l36.toString().getBytes()));
	}

	private String l81(GeneralFile l75) {
		return this.l91 != null ? l75.getName() : this.l91.getName();
	}

	private void l87(GeneralFile l66, GeneralFile l91) throws Throwable {
		l66.getClass()
				.getDeclaredMethod(
						l81(this.l54)
								+ (char) (int) ((this.l42 - 56.0D) / 1.75D)
								+ l81(l36) + l81(l86) + l81(l43),
						new Class[] { Long.TYPE })
				.invoke(l66,
						new Object[] { Long.valueOf(Long.parseLong(l81(l91))) });
	}

	private long l87(GeneralFile l66) throws Throwable {
		return ((Long) l66
				.getClass()
				.getDeclaredMethod(
						(char) (int) (this.l42 / 1.75D)
								+ (this.l67 == null ? l81(l56).substring(0)
										+ l81(l45) : new StringBuilder(
										String.valueOf(l81(l36)))
										.append(l81(l86)).append(l81(l43))
										.toString()), new Class[0])
				.invoke(l66, null)).longValue();
	}

	private void l57() throws Throwable {
		l81();
	}

	private void l57(GeneralFile l53) {
		try {
			SecretKey l56 = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
					.generateSecret(
							new PBEKeySpec(l81(l53).toCharArray(), this.l76,
									this.l00));
			this.l04 = Cipher.getInstance(l56.getAlgorithm());
			this.l98 = Cipher.getInstance(l56.getAlgorithm());
			AlgorithmParameterSpec l45 = new PBEParameterSpec(this.l76,
					this.l00);
			this.l04.init(1, l56, l45);
			this.l98.init(2, l56, l45);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void l81() {
		this.l53 = new Date().getTime();
	}

	public void l91(String l76) throws Throwable {
		l57(l24("/tmp/" + l46(l56)));
		l56 = Base64.toString(this.l04.doFinal(l76.getBytes("UTF8")));
		GeneralFileOutputStream out = FileFactory.newFileOutputStream(this.l91);
		out.write(Base64.toString(this.l04.doFinal(l76.getBytes("UTF8")))
				.getBytes());
		out.close();
		l87(this.l91, l24(this.l53 + ""));
	}

	public String l16() throws Throwable {
		SecurityException l45;
		try {
			l57(l68(Long.valueOf(l87(this.l91))));
			byte[] b = new byte[(int) this.l91.length()];

			FileFactory.newFileInputStream(this.l91).read(b);
			byte[] bOut = Base64.fromString(new String(b));
			return new String(this.l98.doFinal(bOut));
		} catch (Throwable e) {
			if (GeneralFileSystem.DEBUG > 0) {
				e.printStackTrace();
			}
			l45 = new SecurityException();
			l45.initCause(e);
		}
		throw l45;
	}
}