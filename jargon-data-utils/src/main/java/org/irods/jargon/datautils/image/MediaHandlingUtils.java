package org.irods.jargon.datautils.image;

import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.utils.LocalFileUtils;

/**
 * General utilities for dealing with media files.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class MediaHandlingUtils {

	public static final String[] QUICKTIME_MEDIA = { ".aif", ".aiff", ".aac",
			".au", ".gsm", ".mov", ".mid", ".midi", ".mpg", ".mpeg", ".mp4",
			".m4a", ".psd", ".qt", ".qtif", ".qif", ".qti", ".snd", ".tif",
			".tiff", ".wav", ".3g2", ".3pg" };
	public static final String[] FLASH_MEDIA = { ".flv", ".mp3", ".swf" };
	public static final String[] WINDOWS_MEDIA = { ".asx", ".asf", ".avi",
			".wma", ".wmv" };
	public static final String[] IFRAME_CONTENT = { ".html", ".pdf" };

	/**
	 * Right now no public constructor
	 */
	private MediaHandlingUtils() {
	}

	/**
	 * Is the given <code>CollectionAndDataObjectListingEntry</code> an image.
	 * 
	 * @param collectionAndDataObjectListingEntry
	 *            {@link CollectionAndDataObjectListingEntry}
	 * @return <code>boolean</code> if the given object is an image.
	 */
	public static boolean isImageFile(
			final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry) {
		boolean isImage = false;
		if (collectionAndDataObjectListingEntry != null) {
			if (collectionAndDataObjectListingEntry.getObjectType() == ObjectType.DATA_OBJECT) {
				isImage = isImageFile(collectionAndDataObjectListingEntry
						.getFormattedAbsolutePath());
			}
		}
		return isImage;
	}

	/**
	 * Is the given <code>CollectionAndDataObjectListingEntry</code> an image.
	 * 
	 * @param absolutePath
	 *            <code>String</code> assumed to be a data object absolute path
	 * 
	 * @return <code>boolean</code> if the given object is an image.
	 */
	public static boolean isImageFile(final String absolutePath) {
		boolean isImage = false;

		String extension = LocalFileUtils.getFileExtension(absolutePath)
				.toUpperCase();

		if (extension.equals(".JPG") || extension.equals(".GIF")
				|| extension.equals(".PNG") || extension.equals(".TIFF")
				|| extension.equals(".TIF")) {

			isImage = true;
		}

		return isImage;
	}

	/**
	 * Inspects the file extension and determines if the file is one that has an
	 * associated media player. This version returns a simple
	 * <code>boolean</code> value so that image brower applications know that a
	 * media player applies.
	 * 
	 * @param collectionAndDataObjectListingEntry
	 *            {@link CollectionAndDataObjectListingEntry}
	 * @return <code>boolean</code> if the given object is an object that should
	 *         utilize a media player.
	 */
	public static boolean isMediaFile(
			final CollectionAndDataObjectListingEntry collectionAndDataObjectListingEntry) {

		boolean isMedia = false;
		if (collectionAndDataObjectListingEntry != null) {
			if (collectionAndDataObjectListingEntry.getObjectType() == ObjectType.DATA_OBJECT) {
				isMedia = isMediaFile(collectionAndDataObjectListingEntry
						.getPathOrName());
			}
		}
		return isMedia;
	}

	/**
	 * Inspects the file extension and determines if the file is one that has an
	 * associated media player. This version returns a simple
	 * <code>boolean</code> value so that image brower applications know that a
	 * media player applies.
	 * 
	 * @param absolutePath
	 *            <code>String</code> that is assumed to be a data object
	 *            absolute path {@link CollectionAndDataObjectListingEntry}
	 * @return <code>boolean</code> if the given object is an object that should
	 *         utilize a media player.
	 */
	public static boolean isMediaFile(final String absolutePath) {

		boolean isMedia = false;

		// is a data object, classify by extension

		String extension = LocalFileUtils.getFileExtension(absolutePath
				.toLowerCase());

		for (String type : QUICKTIME_MEDIA) {
			if (extension.equals(type)) {
				isMedia = true;
				break;
			}
		}

		if (!isMedia) {
			for (String type : FLASH_MEDIA) {
				if (extension.equals(type)) {
					isMedia = true;
					break;
				}
			}
		}

		if (!isMedia) {
			for (String type : WINDOWS_MEDIA) {
				if (extension.equals(type)) {
					isMedia = true;
					break;
				}
			}
		}

		if (!isMedia) {
			for (String type : IFRAME_CONTENT) {
				if (extension.equals(type)) {
					isMedia = true;
					break;
				}
			}
		}

		return isMedia;
	}
}
