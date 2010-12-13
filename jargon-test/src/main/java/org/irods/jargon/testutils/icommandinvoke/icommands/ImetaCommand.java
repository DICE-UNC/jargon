/**
 *
 */
package org.irods.jargon.testutils.icommandinvoke.icommands;

import org.irods.jargon.testutils.icommandinvoke.IcommandException;

/**
 * Base class for imeta operations
 * 
 * @author Mike Conway, DICE (www.irods.org)
 * 
 */
public abstract class ImetaCommand implements Icommand {

	private String objectPath = "";
	private MetaObjectType metaObjectType = MetaObjectType.DATA_OBJECT_META;

	public enum MetaObjectType {
		USER_META, COLLECTION_META, DATA_OBJECT_META, RESOURCE_META
	}

	public String getObjectPath() {
		return objectPath;
	}

	public void setObjectPath(String objectPath) {
		this.objectPath = objectPath;
	}

	public MetaObjectType getMetaObjectType() {
		return metaObjectType;
	}

	public void setMetaObjectType(MetaObjectType metaObjectType) {
		this.metaObjectType = metaObjectType;
	}

	/**
	 * Get the icommand switch for object type to put into an icommand
	 * @param metaObjectType
	 * @return <code>String</code> with the icommand switch for object type (e.g. -d, -C)
	 * @throws IcommandException
	 */
	public String translateMetaObjectTypeToString(MetaObjectType metaObjectType)
			throws IcommandException {
		String stringMeta;
		switch (metaObjectType) {
		case COLLECTION_META:
			stringMeta = "-C";
			break;
		case USER_META:
			stringMeta = "-u";
			break;
		case DATA_OBJECT_META:
			stringMeta = "-d";
			break;
		case RESOURCE_META:
			stringMeta = "-R";
			break;
		default:
			throw new IcommandException("unknown meta object type");
		}
		return stringMeta;
	}

}
