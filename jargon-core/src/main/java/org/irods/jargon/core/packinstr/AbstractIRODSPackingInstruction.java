/**
 * 
 */
package org.irods.jargon.core.packinstr;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic representation of a packing instruction for the IRODS XML Protocol
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public abstract class AbstractIRODSPackingInstruction implements IRodsPI {

	public static final String KEY_VAL_PAIR_PI = "KeyValPair_PI";
	public static final String SS_LEN = "ssLen";
	public static final String KEYWORD = "keyWord";
	public static final String COND_INPUT = "condInput";
	public static final String S_VALUE = "svalue";
	public static final String INT_PI = "INT_PI";
	public static final String MY_INT = "myInt";
	public static final String L1_DESC_INX = "L1_DESC_INX";

	public static final String INX_VAL_PAIR_PI = "InxValPair_PI";
	public static final String HEADER_PI = "MsgHeader_PI";
	public static final String IS_LEN = "islen";
	public static final String INX = "inx";
	private int apiNumber = 0;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public AbstractIRODSPackingInstruction() {
	}

	/**
	 * Create a set of key value pair tags based on the input list. This is used
	 * internally by many packing instructions.
	 * 
	 * @param kvps
	 *            <code>List<KeyValuePair></code> with the data to be formatted
	 *            as key value pair tags.
	 * @return <code>Tag</code> containing key value pairs.
	 * @throws JargonException
	 */
	protected Tag createKeyValueTag(final List<KeyValuePair> kvps)
			throws JargonException {
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

	/**
	 * Create a SpecColl_PI tag from the given value object data
	 * 
	 * @param specColInfo
	 *            {@link SpecColInfo} parameter holding object
	 * @return {@Tag} representation of the SpecColl_PI tag
	 */
	protected Tag createSpecCollTag(final SpecColInfo specColInfo) {

		if (specColInfo == null) {
			throw new IllegalArgumentException("null specColInfo");
		}

		/*
		 * SpecColl_PI
		 * "int collClass; int type; str collection[MAX_NAME_LEN]; str objPath[MAX_NAME_LEN]; "
		 * +
		 * "str resource[NAME_LEN]; str rescHier[MAX_NAME_LEN]; str phyPath[MAX_NAME_LEN]; "
		 * + "str cacheDir[MAX_NAME_LEN]; int cacheDirty; int replNum;"
		 */

		Tag specCol = new Tag("SpecColl_PI");
		specCol.addTag("collClass", String.valueOf(specColInfo.getCollClass()));
		specCol.addTag("type", String.valueOf(specColInfo.getType()));
		specCol.addTag("collection", specColInfo.getCollection());
		specCol.addTag("objPath", specColInfo.getObjPath());
		specCol.addTag("resource", specColInfo.getResource());

		if (specColInfo.isUseResourceHierarchy()) {
			specCol.addTag("rescHier", "");
		}

		specCol.addTag("phyPath", specColInfo.getPhyPath());
		specCol.addTag("cacheDir", specColInfo.getCacheDir());
		specCol.addTag("cacheDirty",
				String.valueOf(specColInfo.getCacheDirty()));
		// FIXME: add create tag with int value and get rid of these valueOf
		specCol.addTag("replNum", String.valueOf(specColInfo.getReplNum()));
		return specCol;

	}

	/**
	 * Internally used method to format InxValue tags for various packing
	 * instructions.
	 * 
	 * @param ivps
	 *            <code>List<InxVal></code> of data to be formatted as InxVal
	 *            tags.
	 * @return <code>Tag</code> with the InxVal formatted data.
	 * @throws JargonException
	 */
	protected Tag createInxValueTag(final List<InxVal> ivps)
			throws JargonException {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.packinstr.IRodsPI#getParsedTags()
	 */
	@Override
	public String getParsedTags() throws JargonException {

		Tag message = getTagValue();

		if (message == null) {
			return null;
		}

		return message.parseTag();

	}

	/**
	 * Abstract method returns the <code>Tag</code> structure for the given
	 * packing instruction. Implemented by the specific subclass.
	 * 
	 * @return {@link Tag} with the packing instruction as a nested array of tag
	 *         objects.
	 * @throws JargonException
	 */
	public abstract Tag getTagValue() throws JargonException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.packinstr.IRodsPI#getApiNumber()
	 */
	@Override
	public int getApiNumber() {
		return apiNumber;
	}

	/**
	 * @param apiNumber
	 */
	protected void setApiNumber(final int apiNumber) {
		this.apiNumber = apiNumber;
	}

}
