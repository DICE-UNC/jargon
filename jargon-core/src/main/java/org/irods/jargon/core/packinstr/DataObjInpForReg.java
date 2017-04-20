package org.irods.jargon.core.packinstr;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.ProtocolFormException;

/**
 * Translation of a DataObjInp operation to register a path (as in the ireg
 * icommand)
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class DataObjInpForReg extends AbstractIRODSPackingInstruction {

	public static final int OBJ_REG_API_NBR = 630;

	public enum ChecksumHandling {
		NONE, REGISTER_CHECKSUM, VERFIY_CHECKSUM
	}

	private final String physicalFIleAbsolutePath;
	private final String irodsFileAbsolutePath;
	private final String resourceGroup;
	private final String resourceToStoreTo;
	private final boolean force;
	private final boolean recursive;
	private final boolean registerAsReplica;
	private ChecksumHandling checksumHandling;
	private String localFileChecksumValue;

	private int operationType = 0;

	/**
	 * Create an instance for an ireg operation
	 *
	 * @param physicalFileAbsolutePath
	 *            <code>String</code> with the absolute path to the physical
	 *            file to be registered
	 * @param irodsFileAbsolutePath
	 *            <code>String</coce> with the absolute path to the iRODS file to be registered
	 * @param resourceGroup
	 *            <code>String</code>specifies the resource group of the
	 *            resource. This must be input together with the
	 *            resourceToStoreTo
	 * @param resourceToStoreTo
	 *            <code>String</code> specifies the resource to store to. This
	 *            can also be specified in your environment or via a rule set up
	 *            by the administrator
	 * @param force
	 *            <code>boolean</code> indicates overwrite
	 * @param recursive
	 *            <code>boolean</code> which, if <code>true</code>, indicates
	 *            that this is a recursive registration of a collection.
	 * @param checksumHandling
	 *            {@link ChecksumHandling} enum value that indicates the
	 *            approach used for checksums
	 * @param registerAsReplica
	 *            <code>boolean</code> which, if true, registers the file as a
	 *            replica
	 * @param localFileChecksumValue
	 *            <code>String</code> with an optional local file
	 *            checksum,required if the <code>checksumHandling</code>
	 *            indicates that the checksum should be verified. Should be set
	 *            to blank otherwise.
	 * @return {@link DataObjInpForReg}
	 * @throws JargonException
	 */
	public static final DataObjInpForReg instance(
			final String physicalFileAbsolutePath,
			final String irodsFileAbsolutePath, final String resourceGroup,
			final String resourceToStoreTo, final boolean force,
			final boolean recursive, final ChecksumHandling checksumHandling,
			final boolean registerAsReplica, final String localFileChecksumValue)
			throws JargonException {
		return new DataObjInpForReg(physicalFileAbsolutePath,
				irodsFileAbsolutePath, resourceGroup, resourceToStoreTo, force,
				recursive, checksumHandling, registerAsReplica,
				localFileChecksumValue);
	}

	private DataObjInpForReg(final String physicalFileAbsolutePath,
			final String irodsFileAbsolutePath, final String resourceGroup,
			final String resourceToStoreTo, final boolean force,
			final boolean recursive, final ChecksumHandling checksumHandling,
			final boolean registerAsReplica, final String localFileChecksumValue)
			throws JargonException {

		super();
		if (physicalFileAbsolutePath == null
				|| physicalFileAbsolutePath.length() == 0) {
			throw new IllegalArgumentException(
					"physicalFileAbsolutePath is null or empty");
		}

		if (irodsFileAbsolutePath == null
				|| irodsFileAbsolutePath.length() == 0) {
			throw new IllegalArgumentException(
					"irodsFileAbsolutePath is null or empty");
		}

		if (resourceGroup == null) {
			throw new IllegalArgumentException(
					"null resource group, set to blank");
		}

		if (resourceToStoreTo == null) {
			throw new IllegalArgumentException(
					"null resourceToStoreTo, set to blank");
		}

		if (checksumHandling == null) {
			throw new IllegalArgumentException("null checksumHandling");
		}

		if (localFileChecksumValue == null) {
			throw new IllegalArgumentException(
					"null localFileChecksumValue, set to blank if not used");
		}

		if (checksumHandling == ChecksumHandling.VERFIY_CHECKSUM) {
			if (localFileChecksumValue.isEmpty()) {
				throw new ProtocolFormException(
						"verify checksum was indicated, but no local checksum provided");
			}
		}

		if (recursive) {
			if (checksumHandling == ChecksumHandling.VERFIY_CHECKSUM
					|| checksumHandling == ChecksumHandling.REGISTER_CHECKSUM) {
				throw new ProtocolFormException(
						"unable to verify regiseter or verify a checksum when registering a collection");
			}
		}

		physicalFIleAbsolutePath = physicalFileAbsolutePath;
		this.irodsFileAbsolutePath = irodsFileAbsolutePath;
		this.resourceGroup = resourceGroup;
		this.resourceToStoreTo = resourceToStoreTo;
		this.force = force;
		this.recursive = recursive;
		this.checksumHandling = checksumHandling;
		this.registerAsReplica = registerAsReplica;
		this.localFileChecksumValue = localFileChecksumValue;
		setApiNumber(OBJ_REG_API_NBR);
	}

	@Override
	public Tag getTagValue() throws JargonException {

		Tag message = new Tag(DataObjInp.PI_TAG, new Tag[] {
				new Tag(DataObjInp.OBJ_PATH, irodsFileAbsolutePath),
				new Tag(DataObjInp.CREATE_MODE, 0),
				new Tag(DataObjInp.OPEN_FLAGS, 0),
				new Tag(DataObjInp.OFFSET, 0),
				new Tag(DataObjInp.DATA_SIZE, 0),
				new Tag(DataObjInp.NUM_THREADS, 0),
				new Tag(DataObjInp.OPR_TYPE, operationType) });

		List<KeyValuePair> kvps = new ArrayList<KeyValuePair>();
		kvps.add(KeyValuePair.instance("dataType", "generic"));

		kvps.add(KeyValuePair.instance("filePath", physicalFIleAbsolutePath));

		if (recursive) {
			kvps.add(KeyValuePair.instance("collection", ""));
		}

		if (force) {
			kvps.add(KeyValuePair.instance("forceFlag", ""));
		}

		kvps.add(KeyValuePair.instance("destRescName", resourceToStoreTo));

		if (!resourceGroup.isEmpty()) {
			if (resourceToStoreTo.isEmpty()) {
				throw new ProtocolFormException(
						"if a resource group is specified, a resource must be specified");
			}
			kvps.add(KeyValuePair.instance("rescGroupName", resourceGroup));
		}

		if (registerAsReplica) {
			kvps.add(KeyValuePair.instance("regRepl", ""));
		}

		if (checksumHandling == ChecksumHandling.REGISTER_CHECKSUM) {
			kvps.add(KeyValuePair.instance("verifyChksum", ""));
		} else if (checksumHandling == ChecksumHandling.VERFIY_CHECKSUM) {
			kvps.add(KeyValuePair.instance("regChksum", localFileChecksumValue));
		}

		message.addTag(createKeyValueTag(kvps));
		return message;
	}

}
