package org.irods.jargon.core.utils;

import java.util.HashMap;
import java.util.Map;

import org.irods.jargon.core.exception.JargonException;

import edu.sdsc.grid.io.irods.Tag;

/**
 * Utilities for dealing with iRODS XML protocol <code>Tag</code>.
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class TagHandlingUtils {
	
	public static final String KEY_VAL_PAIR_PI = "KeyValPair_PI";
	public static final String SS_LEN = "ssLen";
	
	private TagHandlingUtils() {}

	/**
	 * Given a <code>Tag</code>, extract the key value pairs into a <code>Map</code> for ease of parsing
	 * @param keyValPairPi {@link Tag} that is the KeyValuePair_PI head tag
	 * @return <code>Map<String, String></code> with the key value pairs translated from packing instruction format
	 * @throws JargonException
	 */
	public static Map<String, String> translateKeyValuePairTagIntoMap(final Tag keyValPairPi) throws JargonException {
		
		if (keyValPairPi == null) {
			throw new IllegalArgumentException("null keyValPairPi");
		}
		
		Map<String, String> kvps = new HashMap<String, String>();
		
		if (!(keyValPairPi.getName().equals(KEY_VAL_PAIR_PI))) {
			throw new JargonException("given tag is not a KeyValPair_PI tag");
		}
		
		// get the length
		int kvpLength = keyValPairPi.getTag(SS_LEN).getIntValue();

		for (int i = 1; i < kvpLength; i++) {
			
		}

		return kvps;
		
	}
}
