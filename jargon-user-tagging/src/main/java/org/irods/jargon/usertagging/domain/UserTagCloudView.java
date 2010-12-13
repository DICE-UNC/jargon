/**
 * 
 */
package org.irods.jargon.usertagging.domain;

import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;

import org.irods.jargon.core.exception.JargonException;

/**
 * This class represents a user tag cloud for a given domain (collection, data object, etc) in iRODS.
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public final class UserTagCloudView implements Serializable {
	
	private static final long serialVersionUID = 1130173290281006901L;
	private final String userName;
	private final TreeMap<IRODSTagValue, TagCloudEntry> tagCloudEntries;
	
	/**
	 * Static initializer creates an instance.
	  * @param metadataDomain <code>MetaDataAndDomainData.MetadataDomain</code> enum value that describes the iRODS domain object
	 * being tagged (e.g. collection, data object)
	 * @param userName <code>String</code> with the name of the user who owns the tag cloud.
	 * @param tagCloudEntries <code>List</code> of {@link orgirods.jargon.usertagging.domain.TagCloudEntry} with the summary for the various tags.
	 * @return instance of <code>UserTagCloudView</code>
	 * @throws JargonException
	 */
	public static UserTagCloudView instance(final String userName,  final TreeMap<IRODSTagValue, TagCloudEntry> tagCloudEntries) throws JargonException{
		return new UserTagCloudView(userName, tagCloudEntries);
	}
	
	public static UserTagCloudView instance(final String userName, final List<TagCloudEntry> fileTagCloudEntries, final List<TagCloudEntry> collectionTagCloudEntries) throws JargonException {
		
		if (fileTagCloudEntries == null) {
			throw new JargonException("null fileTagCloudEntries");
		}
		
		if (collectionTagCloudEntries == null) {
			throw new JargonException("Null collectionTagCloudEntries");
		}
		
		TreeMap<IRODSTagValue, TagCloudEntry> tempTreeMap = new TreeMap<IRODSTagValue, TagCloudEntry>();

		// move over the file enties and counts
		for (TagCloudEntry fileEntry : fileTagCloudEntries) {
			tempTreeMap.put(fileEntry.getIrodsTagValue(), new TagCloudEntry(fileEntry.getIrodsTagValue(), fileEntry.getCountOfFiles(), 0));
		}
		
		// update entries with collection counts, if not found, add an entry
		TagCloudEntry foundEntry = null;
		for (TagCloudEntry collectionEntry : collectionTagCloudEntries) {
			foundEntry = tempTreeMap.get(collectionEntry.getIrodsTagValue());
			
			if (foundEntry == null) {
				tempTreeMap.put(collectionEntry.getIrodsTagValue(), new TagCloudEntry(collectionEntry.getIrodsTagValue(), 0,  collectionEntry.getCountOfCollections()));
			} else {
				foundEntry.setCountOfCollections(collectionEntry.getCountOfCollections());
			}
		}
		
		return new UserTagCloudView(userName, tempTreeMap);
		
	}
	
	/**
	 * Private constructor
	 * @param metadataDomain <code>MetaDataAndDomainData.MetadataDomain</code> enum value that describes the iRODS domain object
	 * being tagged (e.g. collection, data object)
	 * @param userName <code>String</code> with the name of the user who owns the tag cloud.
	 * @param tagCloudEntries <code>List</code> of {@link orgirods.jargon.usertagging.domain.TagCloudEntry} with the summary for the various tags.
	 * @throws JargonException
	 */
	private UserTagCloudView(final String userName,  final TreeMap<IRODSTagValue, TagCloudEntry> tagCloudEntries) throws JargonException{
		
		if (userName == null || userName.isEmpty()) {
			throw new JargonException("null or empty userName");
		}
		
		if (tagCloudEntries == null) {
			throw new JargonException("null tagCloudEntries");
		}
		
		this.userName = userName;
		this.tagCloudEntries  = tagCloudEntries;
		
	}
	
	public String getUserName() {
		return userName;
	}
	
	public TreeMap<IRODSTagValue, TagCloudEntry>  getTagCloudEntries() {
		return tagCloudEntries;
	}
	
}
