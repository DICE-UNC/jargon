package org.irods.jargon.datautils.image;

import java.io.File;
import java.io.InputStream;

import org.irods.jargon.core.exception.JargonException;

/**
 * Manage the creation and maintenance of thumbnail images for image files
 * stored in iRODS.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface ThumbnailService {

	/**
	 * Given a <code>File</code> that represents a local working directory, ask iRODS to generate 
	 * a thumbnail image for the iRODS data object at the given absolute path.   This temporary thumbnail
	 * file is then returned to the caller.
	 * <p/>
	 * This method will call an image processing routine on the iRODS server, and the thumbnail data will be streamed back 
	 * to this method.  The resulting thumbnail is stored underneath the temporary thumbnail directory based on an internal
	 * scheme.  The <code>File</code> that is returned from this method points to this thumbnail image. 
	 * 
	 * @param workingDirectory <code>File</code> with the path to the top level of a working directory to hold the
	 * thumbnail image.
	 * @param irodsAbsolutePathToGenerateThumbnailFor <code>String</code> that is the absolute path to the iRODS file
	 * for which a thumbnail will be generated.
	 * @return <code>File</code> that points to the thumbnail image.
	 * @throws JargonException
	 */
	File generateThumbnailForIRODSPath(final File workingDirectory,
			final String irodsAbsolutePathToGenerateThumbnailFor)
			throws JargonException;

	/**
	 * Given an iRODS absolute path to a data object, retrieve an <code>InputStream</code> which is a thumbnail of
	 * the given file at the iRODS path.
	 * <p/>
	 * Currently, this is done by generating the thumbnail when requested, later, this can include a caching scheme,
	 * and alternative cache locations (local verus in iRODS AVU, etc).  Consider this a first approximation.
	 * 
	 * @param workingDirectory <code>File</code> with the path to the top level of a working directory to hold the
	 * thumbnail image.
	 * @param irodsAbsolutePathToGenerateThumbnailFor <code>String</code> that is the absolute path to the iRODS file
	 * for which a thumbnail will be generated.
	 * @return <code>InputStream</code> that is the thumbnail image data.  No buffering is done to the stream that is returned.  
	 * @throws JargonException
	 */
	InputStream retrieveThumbnailByIRODSAbsolutePath(
			final File workingDirectory,
			final String irodsAbsolutePathToGenerateThumbnailFor)
			throws JargonException;

}