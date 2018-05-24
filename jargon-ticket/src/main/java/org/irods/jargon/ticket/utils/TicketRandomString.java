package org.irods.jargon.ticket.utils;

import java.util.Random;

public class TicketRandomString {

	private static final int ALPHA_SET_SIZE = 26;
	private static final int NUMERIC_SET_SIZE = 10;
	private static final int[] symbols = new int[ALPHA_SET_SIZE + ALPHA_SET_SIZE + NUMERIC_SET_SIZE];

	static {
		int j = 0;
		for (int i = 0; i < ALPHA_SET_SIZE; i++) {
			symbols[j++] = 'A' + i;
		}
		for (int i = 0; i < ALPHA_SET_SIZE; i++) {
			symbols[j++] = 'a' + i;
		}
		for (int i = 0; i < NUMERIC_SET_SIZE; i++) {
			symbols[j++] = '0' + i;
		}
	}

	private final Random random = new Random();

	private final char[] buf;

	public TicketRandomString(final int length) {
		if (length < 1) {
			throw new IllegalArgumentException("length < 1: " + length);
		}
		buf = new char[length];
	}

	public String nextString() {
		for (int idx = 0; idx < buf.length; ++idx) {
			buf[idx] = (char) symbols[random.nextInt(symbols.length)];
		}
		return new String(buf);
	}

}
