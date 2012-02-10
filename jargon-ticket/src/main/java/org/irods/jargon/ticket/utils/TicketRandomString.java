package org.irods.jargon.ticket.utils;

import java.util.Random;

public class TicketRandomString {

	private static final int[] symbols = new int[26+26+10];

	static {
		int j=0;
		for (int i=0;i<26;i++) symbols[j++]=(int)'A' + i;
		for (int i=0;i<26;i++) symbols[j++]=(int)'a' + i;
		for (int i=0;i<10;i++) symbols[j++]=(int)'0' + i;
	}

	private final Random random = new Random();

	private final char[] buf;

	public TicketRandomString(int length) {
		if (length < 1)
			throw new IllegalArgumentException("length < 1: " + length);
		buf = new char[length];
	}

	public String nextString() {
		for (int idx = 0; idx < buf.length; ++idx)
			buf[idx] = (char)symbols[random.nextInt(symbols.length)];
		return new String(buf);
	}
	

}
