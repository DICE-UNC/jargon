/**
 *
 */
package org.irods.jargon.vircoll;

/**
 * Abstract model of a virtual collection, which is an arbitrary source that can
 * be serialized into an iRODS file, and which produces an 'ils' like listing.
 * <p/>
 * The function of a virtual collection is to break away from reliance on a
 * hierarchical file tree as the sole arrangement of collections.
 *
 * @author Mike Conway - DICE
 *
 */
public abstract class AbstractVirtualCollection {
	
	/**
	 * The style of paging for this collection
	 * @author Mike Conway - DICE
	 *
	 */
	public enum PagingStyle { NONE, MIXED, SPLIT_COLLECTIONS_AND_FILES }

	public static final String DEFAULT_ICON_KEY = "virtual.collection.default.icon";

	private PagingStyle pagingStyle = PagingStyle.MIXED;
	
	
	/**
	 * Unique name for this virtual collection
	 */
	private String uniqueName = "Collection";

	/**
	 * Plain language descripton
	 */
	private String description = "Collection";

	/**
	 * TODO: extract to a readable interface? iRODS absolute path to the
	 * definition for this virtual collection
	 */
	private String sourcePath = "";

	/**
	 * i18n selector that can be used to name this collection
	 */
	private String i18Name = "virtual.collection.default.name";

	/**
	 * i18n selector that can be used to describe this collection
	 */
	private String i18Description = "virtual.collection.default.description";

	/**
	 * i18n selector that can be mapped to an icon depiction of this collection
	 */
	private String i18icon = DEFAULT_ICON_KEY;

	public String getName() {
		return uniqueName;
	}

	public void setName(final String name) {
		this.uniqueName = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(final String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getI18Name() {
		return i18Name;
	}

	public void setI18Name(final String i18Name) {
		this.i18Name = i18Name;
	}

	public String getI18Description() {
		return i18Description;
	}

	public void setI18Description(final String i18Description) {
		this.i18Description = i18Description;
	}

	public String getI18icon() {
		return i18icon;
	}

	public void setI18icon(final String i18icon) {
		this.i18icon = i18icon;
	}
	
	public PagingStyle getPagingStyle() {
		return pagingStyle;
	}

	public void setPagingStyle(PagingStyle pagingStyle) {
		this.pagingStyle = pagingStyle;
	}


}
