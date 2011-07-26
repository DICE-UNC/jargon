package org.irods.jargon.core.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Wrapper around RandomAccessFile intended to increase performance of parallel
 * io processing.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ReadRandomFile {
	private static final int DEFAULT_BUFSIZE = 4096;

	private RandomAccessFile raf;
	private byte inbuf[];
	private long startpos = -1;
	private long endpos = -1;
	private int bufsize;

	public ReadRandomFile(final String name) throws FileNotFoundException {
		this(name, DEFAULT_BUFSIZE);
	}

	public ReadRandomFile(final String name, final int b)
			throws FileNotFoundException {
		raf = new RandomAccessFile(name, "r");
		bufsize = b;
		inbuf = new byte[bufsize];
	}

	public ReadRandomFile(final RandomAccessFile randomAccessFile, final int b)
			throws FileNotFoundException {
		raf = randomAccessFile;
		bufsize = b;
		inbuf = new byte[bufsize];
	}

	public int read(final long pos) {
		if (pos < startpos || pos > endpos) {
			long blockstart = (pos / bufsize) * bufsize;
			int n;
			try {
				raf.seek(blockstart);
				n = raf.read(inbuf);
			} catch (IOException e) {
				return -1;
			}
			startpos = blockstart;
			endpos = blockstart + n - 1;
			if (pos < startpos || pos > endpos) {
				return -1;
			}
		}

		return inbuf[(int) (pos - startpos)] & 0xffff;
	}

	public void close() throws IOException {
		raf.close();
	}

	/*
	 * public static void main(final String args[]) { if (args.length != 1) {
	 * System.err.println("missing filename"); System.exit(1); }
	 * 
	 * try { ReadRandomFile rr = new ReadRandomFile(args[0]); long pos = 0; int
	 * c; byte buf[] = new byte[1]; while ((c = rr.read(pos)) != -1) { pos++;
	 * buf[0] = (byte) c; System.out.write(buf, 0, 1); } rr.close(); } catch
	 * (IOException e) { System.err.println(e); } }
	 */
}
