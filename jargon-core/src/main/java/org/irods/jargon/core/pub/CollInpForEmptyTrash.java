/**
 * 
 */
package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.AbstractIRODSPackingInstruction;
import org.irods.jargon.core.packinstr.CollInp;
import org.irods.jargon.core.packinstr.KeyValuePair;
import org.irods.jargon.core.packinstr.Tag;

/**
 * CollInp packing instruction for removing trash
 * 
 * @author conwaymc
 *
 */
public class CollInpForEmptyTrash extends AbstractIRODSPackingInstruction {

	public enum TrashOperationMode {
		USER, ADMIN
	}

	public static final String ADMIN_RMTRASH_KW = "irodsAdminRmTrash";
	public static final String RMTRASH_KW = "irodsRmTrash";
	public static final String ZONE_KW = "zone";
	public static final String AGE_KW = "age";

	public static final String PI_TAG = CollInp.PI_TAG;
	/**
	 * Detailed operation flags
	 */
	private final TrashOptions trashOptions;
	/**
	 * Collection name to removed must be properly resolved (eg orpan versus home)
	 * at call time. The user name must be in the proper user#zone format for remote
	 * operations, this class does no user calculation.
	 */
	private final String collectionName;

	/**
	 * If a remote zone is involved, it must be set here, otherwise it can be left
	 * blank
	 */
	private final String zone;

	@Override
	public Tag getTagValue() throws JargonException {
		int oprType = 0;

		Tag message = new Tag(PI_TAG, new Tag[] { new Tag(CollInp.COLL_NAME, collectionName), new Tag(CollInp.FLAGS, 0),
				new Tag(CollInp.OPR_TYPE, oprType) });

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();

		kvps.add(KeyValuePair.instance(CollInp.FORCE_FLAG, ""));

		if (trashOptions.isRecursive()) {
			kvps.add(KeyValuePair.instance(CollInp.RECURSIVE_OPR, ""));
		}

		if (trashOptions.getTrashOperationMode() == TrashOperationMode.ADMIN) {
			kvps.add(KeyValuePair.instance(ADMIN_RMTRASH_KW, ""));
		} else {
			kvps.add(KeyValuePair.instance(RMTRASH_KW, ""));
		}

		/*
		 * If in a remote zone a zone kw needs to be supplied
		 * 
		 */

		if (!zone.isEmpty()) {
			kvps.add(KeyValuePair.instance(ZONE_KW, zone));

		}

		/*
		 * Age is added if > 0
		 */

		if (trashOptions.getAgeInMinutes() > 0) {
			kvps.add(KeyValuePair.instance(AGE_KW, String.valueOf(trashOptions.getAgeInMinutes())));

		}

		message.addTag(createKeyValueTag(kvps));
		return message;
	}

	/**
	 * Constructor with all required values
	 * 
	 * @param trashOptions
	 *            {@link TrashOptions} with processing flags and details
	 * @param collectionName
	 *            {@link String} with the optional (blank if not passed) collection
	 *            name
	 * @param zone
	 *            {@link String} with an optional (blank if not passed) zone for the
	 *            operation
	 */
	public CollInpForEmptyTrash(TrashOptions trashOptions, String collectionName, String zone) {
		super();
		if (trashOptions == null) {
			throw new IllegalArgumentException("null trashOptions");
		}

		if (collectionName == null || collectionName.isEmpty()) {
			throw new IllegalArgumentException("null or empty collection name");
		}

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		this.trashOptions = trashOptions;
		this.collectionName = collectionName;
		this.zone = zone;
	}

}
