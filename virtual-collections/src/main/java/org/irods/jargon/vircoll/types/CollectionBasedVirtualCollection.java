/**
 *
 */
package org.irods.jargon.vircoll.types;

import org.irods.jargon.vircoll.AbstractVirtualCollection;

/**
 * Basic definition of a virtual collection that is actually a collection from
 * the iRODS hierarchy. It's simply an iRODS collection
 *
 * @author Mike Conway - DICE
 *
 */
public class CollectionBasedVirtualCollection extends AbstractVirtualCollection {

	public static final String DESCRIPTION_KEY_HOME = "virtual.collection.description.home";
	public static final String DESCRIPTION_KEY_ROOT = "virtual.collection.description.root";
	public static final String DESCRIPTION = "iRODS Collection at a given path";

	/**
	 * Represents the iRODS absolute path that is the parent of this virtual
	 * collection. This type of virtual collection actually just represents and
	 * iRODS collection to put it on an equal footing with a collection derived
	 * from a query
	 */
	private String rootPath = "";

	/**
	 * create an instance of this virtual collection by giving the iRODS parent
	 * path that will be the root of the collection listing
	 */
	public CollectionBasedVirtualCollection(final String uniqueName,
			final String rootPath) {
		if (rootPath == null || rootPath.isEmpty()) {
			throw new IllegalArgumentException("null root path");
		}

		if (uniqueName == null || uniqueName.isEmpty()) {
			throw new IllegalArgumentException("null uniqueName");
		}

		this.rootPath = rootPath;
		setName(rootPath);
		setDescription(DESCRIPTION);
		setI18icon(DEFAULT_ICON_KEY);

	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(final String collectionPath) {
		rootPath = collectionPath;
	}

}
