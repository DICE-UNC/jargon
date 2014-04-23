/**
 *
 */
package org.irods.jargon.vircoll.types;

import org.irods.jargon.core.query.PagingAwareCollectionListing.PagingStyle;
import org.irods.jargon.vircoll.AbstractVirtualCollection;

/**
 * Represents a collection of starred files and folders
 *
 * @author Mike Conway - DICE
 *
 */
public class StarredFoldersVirtualCollection extends AbstractVirtualCollection {

	public static final String DESCRIPTION_KEY = "virtual.collection.description.starred";
	public static final String DESCRIPTION = "Files and folders marked as starred in iRODS";
	public static final String NAME = "Starred Files";
	public static final String NAME_KEY = "virtual.collection.name.starred";
	public static final String ICON_KEY = "virtual.collection.icon.starred";

	/**
	 * create an instance of a starred virtual collection
	 */
	public StarredFoldersVirtualCollection() {
		setName(NAME);
		setDescription(DESCRIPTION);
		setI18icon(ICON_KEY);
		setI18Description(DESCRIPTION_KEY);
		setI18Name(NAME_KEY);
		setPagingStyle(PagingStyle.MIXED);

	}
}
