package org.irods.jargon.datautils.image;

import java.io.File;
import java.io.InputStream;

import org.irods.jargon.core.exception.JargonException;

/**
 * Manage the creation and maintenance of thumbnail images for image files
 * stored in iRODS.
 * 
 * Note that local processing of tiff files requires JAI - see
 * http://java.net/projects/jai-core/ and see
 * http://java.sun.com/products/java-media/jai/downloads/download-1_1_2.html for
 * download The JAI libraries may need to be installed on the client or mid-tier
 * machine
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface ThumbnailService {

	public static final String THUMBNAIL_RULE_DATA_PARAMETER = "*StdoutStr";

	/**
	 * Given a {@code File} that represents a local working directory, ask iRODS to
	 * generate a thumbnail image for the iRODS data object at the given absolute
	 * path. This temporary thumbnail file is then returned to the caller.
	 * <p>
	 * This method will call an image processing routine on the iRODS server, and
	 * the thumbnail data will be streamed back to this method. The resulting
	 * thumbnail is stored underneath the temporary thumbnail directory based on an
	 * internal scheme. The {@code File} that is returned from this method points to
	 * this thumbnail image.
	 * 
	 * @param workingDirectory
	 *            {@code File} with the path to the top level of a working directory
	 *            to hold the thumbnail image.
	 * @param irodsAbsolutePathToGenerateThumbnailFor
	 *            {@code String} that is the absolute path to the iRODS file for
	 *            which a thumbnail will be generated.
	 * @return {@code File} that points to the thumbnail image.
	 * @throws IRODSThumbnailProcessUnavailableException
	 *             if thumbnail processing is not set up on iRODS (imagemagik
	 *             services)
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	File generateThumbnailForIRODSPathViaRule(final File workingDirectory,
			final String irodsAbsolutePathToGenerateThumbnailFor)
			throws IRODSThumbnailProcessUnavailableException, JargonException;

	/**
	 * Given an iRODS absolute path to a data object, retrieve an
	 * {@code InputStream} which is a thumbnail of the given file at the iRODS path.
	 * <p>
	 * Currently, this is done by generating the thumbnail when requested, later,
	 * this can include a caching scheme, and alternative cache locations (local
	 * verus in iRODS AVU, etc). Consider this a first approximation.
	 * 
	 * @param irodsAbsolutePathToGenerateThumbnailFor
	 *            {@code String} that is the absolute path to the iRODS file for
	 *            which a thumbnail will be generated.
	 * @return {@code InputStream} that is the thumbnail image data. No buffering is
	 *         done to the stream that is returned.
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	InputStream retrieveThumbnailByIRODSAbsolutePathViaRule(

			final String irodsAbsolutePathToGenerateThumbnailFor) throws JargonException;

	/**
	 * Do a check to see whether the thumbnail service is available on the iRODS
	 * server. If it is not available, the mid-tier fallback can be used
	 * {@code createThumbnailLocally()}.
	 * <p>
	 * Note that it is not efficient to call this method repeatedly, rather, a
	 * client service should call once for an iRODS server and cache the result.
	 * 
	 * @return {@code true} if the iRODS server has support for imagemagik thumbnail
	 *         generation. If the hueristic cannot determine, it will return false.
	 *         The current heuristic is to use the listCommands.sh script, which
	 *         must be added to the /server/bin/cmd directory, along with the
	 *         makeThumbnail.py script.
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	boolean isIRODSThumbnailGeneratorAvailable() throws JargonException;

}
