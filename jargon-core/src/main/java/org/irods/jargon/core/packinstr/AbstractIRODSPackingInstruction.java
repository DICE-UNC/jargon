/**
 * 
 */
package org.irods.jargon.core.packinstr;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.sdsc.grid.io.irods.Tag;

/**
 * Generic representation of a packing instruction fo rhte IRODS XML Protocol
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractIRODSPackingInstruction implements IRodsPI {

	public static final String KEY_VAL_PAIR_PI = "KeyValPair_PI";
	public static final String SS_LEN = "ssLen";
	public static final String KEYWORD = "keyWord";
	public static final String S_VALUE = "svalue";
	public static final String INT_PI = "INT_PI";
	public static final String MY_INT = "myInt";
	public static final String L1_DESC_INX = "l1descInx";

	public static final String INX_VAL_PAIR_PI = "InxValPair_PI";
	public static final String IS_LEN = "islen";
	public static final String INX = "inx";
	private int apiNumber = 0;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public AbstractIRODSPackingInstruction() {
	}

	Tag createKeyValueTag(final List<KeyValuePair> kvps) throws JargonException {
		/*
		 * Must be like the following: <KeyValPair_PI> <ssLen>3</ssLen>
		 * <keyWord>dataType</keyWord> <keyWord>destRescName</keyWord>
		 * <keyWord>dataIncluded</keyWord> <svalue>generic</svalue>
		 * <svalue>resourceB</svalue> <svalue></svalue> </KeyValPair_PI>
		 */

		if (kvps == null) {
			throw new JargonException("kvps are null");
		}

		Tag pair = new Tag(KEY_VAL_PAIR_PI, new Tag(SS_LEN, 0));
		int ssLength = 0;

		// return the empty Tag if nothing was passed in
		if (kvps.size() == 0) {
			return pair;
		}

		// add keys
		for (KeyValuePair kvp : kvps) {
			pair.addTag(KEYWORD, kvp.getKey());
			ssLength++;
		}

		// just use index zero because they have to be in order...
		pair.getTags()[0].setValue(ssLength);
		if (ssLength == 0) {
			return pair;
		}

		// add values
		for (KeyValuePair kvp : kvps) {
			pair.addTag(S_VALUE, kvp.getValue());
		}

		if (log.isDebugEnabled()) {
			log.debug("kvp tag: {}", pair.parseTag());
		}

		return pair;
	}

	Tag createInxValueTag(final List<InxVal> ivps) throws JargonException {

		/*
		 * A key/value pair with an integer key and a string value #define
		 * InxValPair_PI "int isLen; int *inx(isLen); str *svalue[isLen];"
		 */

		if (ivps == null) {
			throw new JargonException("ivps is null");
		}

		Tag pair = new Tag(INX_VAL_PAIR_PI, new Tag(IS_LEN, 0));
		int isLength = 0;

		// return the empty Tag if nothing was passed in
		if (ivps.size() == 0) {
			return pair;
		}

		// add keys
		for (InxVal ivp : ivps) {
			pair.addTag(INX, ivp.getName().toString());
			isLength++;
		}

		// just use index zero because they have to be in order...
		pair.getTags()[0].setValue(isLength);
		if (isLength == 0) {
			return pair;
		}

		// add values
		for (InxVal ivp : ivps) {
			pair.addTag(S_VALUE, ivp.getValue());
		}

		if (log.isDebugEnabled()) {
			log.debug("ivp tag: {}", pair.parseTag());
		}

		return pair;
	}

	@Override
	public String getParsedTags() throws JargonException {

		Tag message = getTagValue();

		String tagOut = message.parseTag();

		if (log.isDebugEnabled()) {
			log.debug("tag created:" + tagOut);
		}

		return tagOut;

	}

	public abstract Tag getTagValue() throws JargonException;

	@Override
	public int getApiNumber() {
		return apiNumber;
	}

	protected void setApiNumber(final int apiNumber) {
		this.apiNumber = apiNumber;
	}

}
