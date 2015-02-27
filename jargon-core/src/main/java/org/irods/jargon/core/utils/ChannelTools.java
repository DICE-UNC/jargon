package org.irods.jargon.core.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class ChannelTools {
	private static final Logger log = LoggerFactory
			.getLogger(ChannelTools.class);

	public static void fastChannelCopy(final ReadableByteChannel src,
			final WritableByteChannel dest, final int bufferSize)
					throws IOException {
		log.info("fast channel copy on streams, buffer length set to:{}",
				bufferSize);
		final ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
		while (src.read(buffer) != -1) {
			// prepare the buffer to be drained
			buffer.flip();
			// write to the channel, may block
			dest.write(buffer);
			// If partial transfer, shift remainder down
			// If buffer is empty, same as doing clear()
			buffer.compact();
		}

		log.info("eof on stream");

		// EOF will leave buffer in fill state
		buffer.flip();
		// make sure the buffer is fully drained.
		while (buffer.hasRemaining()) {
			dest.write(buffer);
		}
	}
}