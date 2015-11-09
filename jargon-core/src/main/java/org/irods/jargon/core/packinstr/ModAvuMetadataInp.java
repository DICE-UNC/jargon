/**
 *
 */
package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.AvuData;

/**
 * Translation of a ModifyAvuMetadataInp operation into XML protocol format.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ModAvuMetadataInp extends AbstractIRODSPackingInstruction {

	public static final String PI_TAG = "ModAVUMetadataInp_PI";
	public static final String ARG_PREFIX = "arg";

	public static final int MOD_AVU_API_NBR = 706;

	/**
	 * Type of metadata to be modified
	 *
	 */
	public enum MetadataTargetType {
		RESOURCE, USER, COLLECTION, DATA_OBJECT
	}

	/**
	 * Modify action
	 */
	public enum ActionType {
		ADD, REMOVE, MOD
	}

	private final String targetIdentifier;
	private final MetadataTargetType metadataTargetType;
	private final AvuData avuData;
	private final AvuData newAvuData;
	private final ActionType actionType;

	/**
	 * Create an instance of the packing instruction that will add the AVU to a
	 * collection.
	 *
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 */
	public static final ModAvuMetadataInp instanceForAddCollectionMetadata(
			final String targetIdentifier, final AvuData avuData) {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.COLLECTION, avuData, null, ActionType.ADD);
	}

	/**
	 * Create an instance of the packing instruction that will modify the AVU on
	 * a collection.
	 *
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 */
	public static final ModAvuMetadataInp instanceForModifyCollectionMetadata(
			final String targetIdentifier, final AvuData avuData,
			final AvuData newAvuData) {

		if (newAvuData == null) {
			throw new IllegalArgumentException("Null newAvuData");
		}

		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.COLLECTION, avuData, newAvuData,
				ActionType.MOD);
	}

	/**
	 * Create an instance of the packing instruction that will add the AVU to a
	 * collection.
	 *
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 */
	public static final ModAvuMetadataInp instanceForDeleteCollectionMetadata(
			final String targetIdentifier, final AvuData avuData) {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.COLLECTION, avuData, null, ActionType.REMOVE);
	}

	/**
	 * Create an instance of the packing instruction that will add the AVU to a
	 * data object.
	 *
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 */
	public static final ModAvuMetadataInp instanceForAddDataObjectMetadata(
			final String targetIdentifier, final AvuData avuData) {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.DATA_OBJECT, avuData, null, ActionType.ADD);
	}

	/**
	 * Create an instance of the packing instruction that will modify the AVU on
	 * a data object.
	 *
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 */
	public static final ModAvuMetadataInp instanceForModifyDataObjectMetadata(
			final String targetIdentifier, final AvuData avuData,
			final AvuData newAvuData) {

		if (newAvuData == null) {
			throw new IllegalArgumentException("Null newAvuData");
		}

		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.DATA_OBJECT, avuData, newAvuData,
				ActionType.MOD);
	}

	/**
	 * Create an instance of the packing instruction that will add the AVU to a
	 * data object.
	 *
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 */
	public static final ModAvuMetadataInp instanceForDeleteDataObjectMetadata(
			final String targetIdentifier, final AvuData avuData) {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.DATA_OBJECT, avuData, null,
				ActionType.REMOVE);
	}

	/**
	 * Create an instance of the packing instruction that will add the AVU to a
	 * resource.
	 *
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 */
	public static final ModAvuMetadataInp instanceForAddResourceMetadata(
			final String targetIdentifier, final AvuData avuData) {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.RESOURCE, avuData, null, ActionType.ADD);
	}

	/**
	 * Create an instance of the packing instruction that will modify the AVU on
	 * a resource.
	 *
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 */
	public static final ModAvuMetadataInp instanceForModifyResourceMetadata(
			final String targetIdentifier, final AvuData avuData,
			final AvuData newAvuData) {

		if (newAvuData == null) {
			throw new IllegalArgumentException("Null newAvuData");
		}

		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.RESOURCE, avuData, newAvuData,
				ActionType.MOD);
	}

	/**
	 * Create an instance of the packing instruction that will remove the AVU
	 * from a resource .
	 *
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 */
	public static final ModAvuMetadataInp instanceForDeleteResourceMetadata(
			final String targetIdentifier, final AvuData avuData) {
		return new ModAvuMetadataInp(targetIdentifier,
				MetadataTargetType.RESOURCE, avuData, null, ActionType.REMOVE);
	}

	/**
	 * Create an instance of the packing instruction that will add the AVU to a
	 * user.
	 *
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 */
	public static final ModAvuMetadataInp instanceForAddUserMetadata(
			final String targetIdentifier, final AvuData avuData) {
		return new ModAvuMetadataInp(targetIdentifier, MetadataTargetType.USER,
				avuData, null, ActionType.ADD);
	}

	/**
	 * Create an instance of the packing instruction that will modify the AVU on
	 * a user.
	 *
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 */
	public static final ModAvuMetadataInp instanceForModifyUserMetadata(
			final String targetIdentifier, final AvuData avuData,
			final AvuData newAvuData) {

		if (newAvuData == null) {
			throw new IllegalArgumentException("Null newAvuData");
		}

		return new ModAvuMetadataInp(targetIdentifier, MetadataTargetType.USER,
				avuData, newAvuData, ActionType.MOD);
	}

	/**
	 * Create an instance of the packing instruction that will remove the AVU
	 * from a user .
	 *
	 * @param targetIdentifier
	 *            <code>String</code> with the path or unique name of the object
	 *            to which the metadata will be added
	 * @return
	 */
	public static final ModAvuMetadataInp instanceForDeleteUserMetadata(
			final String targetIdentifier, final AvuData avuData) {
		return new ModAvuMetadataInp(targetIdentifier, MetadataTargetType.USER,
				avuData, null, ActionType.REMOVE);
	}

	private ModAvuMetadataInp(final String targetIdentifier,
			final MetadataTargetType metadataTargetType, final AvuData avuData,
			final AvuData newAvuData, final ActionType actionType) {
		super();

		if (targetIdentifier == null || targetIdentifier.isEmpty()) {
			throw new IllegalArgumentException(
					"null or empty target identifier");
		}

		if (metadataTargetType == null) {
			throw new IllegalArgumentException("metadataTargetType is null");
		}

		if (avuData == null) {
			throw new IllegalArgumentException("null or missing avuData");
		}

		if (actionType == null) {
			throw new IllegalArgumentException("null action type");
		}

		this.targetIdentifier = targetIdentifier;
		this.metadataTargetType = metadataTargetType;
		this.avuData = avuData;
		this.actionType = actionType;
		this.newAvuData = newAvuData;

		setApiNumber(MOD_AVU_API_NBR);

	}

	@Override
	public Tag getTagValue() throws JargonException {

		List<String> argList = new ArrayList<String>();
		Tag message = new Tag(PI_TAG);

		if (actionType == ActionType.ADD) {
			argList.add("add");
		} else if (actionType == ActionType.REMOVE) {
			argList.add("rmw");
		} else if (actionType == ActionType.MOD) {
			argList.add("mod");
		}

		if (metadataTargetType == MetadataTargetType.COLLECTION) {
			argList.add("-c");
		} else if (metadataTargetType == MetadataTargetType.DATA_OBJECT) {
			argList.add("-d");
		} else if (metadataTargetType == MetadataTargetType.RESOURCE) {
			argList.add("-R");
		} else if (metadataTargetType == MetadataTargetType.USER) {
			argList.add("-u");
		} else {
			throw new JargonException(
					"metadata target type is not currently supported:"
							+ metadataTargetType);
		}

		argList.add(targetIdentifier);

		// add the AVU elements

		argList.add(avuData.getAttribute());
		argList.add(avuData.getValue());
		if (!avuData.getUnit().isEmpty()) {
			argList.add(avuData.getUnit());
		}

		if (actionType == ActionType.MOD) {
			StringBuilder sb = new StringBuilder();
			// attrib
			sb.append("n:");
			sb.append(newAvuData.getAttribute());
			argList.add(sb.toString());
			// value
			sb = new StringBuilder();
			sb.append("v:");
			sb.append(newAvuData.getValue());
			argList.add(sb.toString());
			// unit
			sb = new StringBuilder();
			sb.append("u:");
			sb.append(newAvuData.getUnit());
			argList.add(sb.toString());

		}

		StringBuilder argBuilder;
		String val = "";
		for (int i = 0; i < 10; i++) {
			argBuilder = new StringBuilder(ARG_PREFIX);
			argBuilder.append(i);
			val = "";
			if (i < argList.size()) {
				val = argList.get(i);
			}
			message.addTag(argBuilder.toString(), val);
		}

		// take the arg list and compact the params

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

	public AvuData getNewAvuData() {
		return newAvuData;
	}

}
