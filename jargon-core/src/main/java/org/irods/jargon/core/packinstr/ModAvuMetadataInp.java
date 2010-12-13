/**
 * 
 */
package org.irods.jargon.core.packinstr;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AvuData;

import edu.sdsc.grid.io.irods.Tag;

/**
 * Translation of a ModifyAvuMetadataInp operation into XML protocol format.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class ModAvuMetadataInp extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "ModAVUMetadataInp_PI";

	public static final String ARG0 = "arg0";
	public static final String ARG1 = "arg1";
	public static final String ARG2 = "arg2";
	public static final String ARG3 = "arg3";
	public static final String ARG4 = "arg4";
	public static final String ARG5 = "arg5";
	public static final String ARG6 = "arg6";
	public static final String ARG7 = "arg7";
	public static final String ARG8 = "arg8";
	public static final String ARG9 = "arg9";

	public static final String ARG_PREFIX = "arg";

	public static final int MOD_AVU_API_NBR = 706;

	public enum MetadataTargetType {
		RESOURCE, USER, COLLECTION, DATA_OBJECT
	};

	public enum ActionType {
		ADD, REMOVE, MOD
	}

	private final String targetIdentifier;
	private final MetadataTargetType metadataTargetType;
	private final AvuData avuData;
	private final ActionType actionType;

	/**
	 * Create an instance of the packing instruction that will add the AVU to a
	 * collection.
	 * 
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 * @throws JargonException
	 */
	public static final ModAvuMetadataInp instanceForAddCollectionMetadata(
			final String targetIdentifier, final AvuData avuData)
			throws JargonException {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.COLLECTION, avuData, ActionType.ADD);
	}

	/**
	 * Create an instance of the packing instruction that will modify the AVU on
	 * a collection.
	 * 
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 * @throws JargonException
	 */
	public static final ModAvuMetadataInp instanceForModifyCollectionMetadata(
			final String targetIdentifier, final AvuData avuData)
			throws JargonException {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.COLLECTION, avuData, ActionType.MOD);
	}

	/**
	 * Create an instance of the packing instruction that will add the AVU to a
	 * collection.
	 * 
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 * @throws JargonException
	 */
	public static final ModAvuMetadataInp instanceForDeleteCollectionMetadata(
			final String targetIdentifier, final AvuData avuData)
			throws JargonException {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.COLLECTION, avuData, ActionType.REMOVE);
	}

	/**
	 * Create an instance of the packing instruction that will add the AVU to a
	 * data object.
	 * 
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 * @throws JargonException
	 */
	public static final ModAvuMetadataInp instanceForAddDataObjectMetadata(
			final String targetIdentifier, final AvuData avuData)
			throws JargonException {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.DATA_OBJECT, avuData, ActionType.ADD);
	}

	/**
	 * Create an instance of the packing instruction that will modify the AVU on
	 * a data object.
	 * 
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 * @throws JargonException
	 */
	public static final ModAvuMetadataInp instanceForModifyDataObjectMetadata(
			final String targetIdentifier, final AvuData avuData)
			throws JargonException {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.DATA_OBJECT, avuData, ActionType.MOD);
	}

	/**
	 * Create an instance of the packing instruction that will add the AVU to a
	 * data object.
	 * 
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 * @throws JargonException
	 */
	public static final ModAvuMetadataInp instanceForDeleteDataObjectMetadata(
			final String targetIdentifier, final AvuData avuData)
			throws JargonException {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.DATA_OBJECT, avuData, ActionType.REMOVE);
	}

	/**
	 * Create an instance of the packing instruction that will add the AVU to a
	 * resource.
	 * 
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 * @throws JargonException
	 */
	public static final ModAvuMetadataInp instanceForAddResourceMetadata(
			final String targetIdentifier, final AvuData avuData)
			throws JargonException {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.RESOURCE, avuData, ActionType.ADD);
	}

	/**
	 * Create an instance of the packing instruction that will modify the AVU on
	 * a resource.
	 * 
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 * @throws JargonException
	 */
	public static final ModAvuMetadataInp instanceForModifyResourceMetadata(
			final String targetIdentifier, final AvuData avuData)
			throws JargonException {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.RESOURCE, avuData, ActionType.MOD);
	}

	/**
	 * Create an instance of the packing instruction that will remove the AVU
	 * from a resource .
	 * 
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 * @throws JargonException
	 */
	public static final ModAvuMetadataInp instanceForDeleteResourceMetadata(
			final String targetIdentifier, final AvuData avuData)
			throws JargonException {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.RESOURCE, avuData, ActionType.REMOVE);
	}

	/**
	 * Create an instance of the packing instruction that will add the AVU to a
	 * user.
	 * 
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 * @throws JargonException
	 */
	public static final ModAvuMetadataInp instanceForAddUserMetadata(
			final String targetIdentifier, final AvuData avuData)
			throws JargonException {
		return new ModAvuMetadataInp(targetIdentifier, MetadataTargetType.USER,
				avuData, ActionType.ADD);
	}

	/**
	 * Create an instance of the packing instruction that will modify the AVU on
	 * a user.
	 * 
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 * @throws JargonException
	 */
	public static final ModAvuMetadataInp instanceForModifyUserMetadata(
			final String targetIdentifier, final AvuData avuData)
			throws JargonException {
		return new ModAvuMetadataInp(targetIdentifier, MetadataTargetType.USER,
				avuData, ActionType.MOD);
	}

	/**
	 * Create an instance of the packing instruction that will remove the AVU
	 * from a user .
	 * 
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 * @throws JargonException
	 */
	public static final ModAvuMetadataInp instanceForDeleteUserMetadata(
			final String targetIdentifier, final AvuData avuData)
			throws JargonException {
		return new ModAvuMetadataInp(targetIdentifier, MetadataTargetType.USER,
				avuData, ActionType.REMOVE);
	}

	private ModAvuMetadataInp(final String targetIdentifier,
			final MetadataTargetType metadataTargetType, final AvuData avuData,
			final ActionType actionType) throws JargonException {
		super();

		if (targetIdentifier == null || targetIdentifier.isEmpty()) {
			throw new JargonException("null or empty target identifier");
		}

		if (metadataTargetType == null) {
			throw new JargonException("metadataTargetType is null");
		}

		if (avuData == null) {
			throw new JargonException("null or missing avuData");
		}

		if (actionType == null) {
			throw new JargonException("null action type");
		}

		this.targetIdentifier = targetIdentifier;
		this.metadataTargetType = metadataTargetType;
		this.avuData = avuData;
		this.actionType = actionType;

		this.setApiNumber(MOD_AVU_API_NBR);

	}

	@Override
	public Tag getTagValue() throws JargonException {
		Tag message = new Tag(PI_TAG);

		if (actionType == ActionType.ADD) {
			message.addTag(ARG0, "add");
		} else if (actionType == ActionType.REMOVE) {
			message.addTag(ARG0, "rmw");
		} else if (actionType == ActionType.MOD) {
			message.addTag(ARG0, "mod");
		}

		if (metadataTargetType == MetadataTargetType.COLLECTION) {
			message.addTag(ARG1, "-c");
		} else if (metadataTargetType == MetadataTargetType.DATA_OBJECT) {
			message.addTag(ARG1, "-d");
		} else if (metadataTargetType == MetadataTargetType.RESOURCE) {
			message.addTag(ARG1, "-R");
		} else if (metadataTargetType == MetadataTargetType.USER) {
			message.addTag(ARG1, "-u");
		} else {
			throw new JargonException(
					"metadata target type is not currently supported:"
							+ metadataTargetType);
		}

		message.addTag(ARG2, targetIdentifier);

		// add the AVU elements

		message.addTag(ARG3, avuData.getAttribute());
		message.addTag(ARG4, avuData.getValue());
		message.addTag(ARG5, avuData.getUnit());
		// filler
		message.addTag(ARG6, "");
		message.addTag(ARG7, "");
		message.addTag(ARG8, "");
		message.addTag(ARG9, "");
		return message;

	}

	public String getTargetIdentifier() {
		return targetIdentifier;
	}

	public MetadataTargetType getMetadataTargetType() {
		return metadataTargetType;
	}

	public AvuData getAvuData() {
		return avuData;
	}

	public ActionType getActionType() {
		return actionType;
	}

}
