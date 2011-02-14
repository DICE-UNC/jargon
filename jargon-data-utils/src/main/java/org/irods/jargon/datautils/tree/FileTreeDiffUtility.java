package org.irods.jargon.datautils.tree;

import java.io.File;

import org.irods.jargon.core.exception.JargonException;

public interface FileTreeDiffUtility {

	/**
	 * Generate a tree model that is the diff between the left hand side and the
	 * iRODS file system on the right hand side. This diff tree will depict the
	 * common directory structure and indicate places where files or collections
	 * differ from the perspective of the 'left hand side' file.
	 * <p/>
	 * The <code>FileTreeDiffEntry</code> will indicate the type of difference
	 * found. This is the user data object that is kept in a
	 * <code>FileTreeNode</code> in the resulting <code>FileTreeModel</code>.
	 * <p/>
	 * This utility will compare file-to-file for data objects. Since local and
	 * iRODS time stamps are not a good indicator for direct comparison, the
	 * utility compares two file (data objects) based on length. Optionally, the
	 * iRODS timestamp can be compared to a provided timestamp value to treat
	 * files modified after the given cut-off as 'changed'.
	 * 
	 * @param localFileRoot
	 *            <code>File</code> that is the left hand side of the comparison
	 * 
	 * @param irodsAbsolutePath
	 *            <code>String</code> that is the root directory that will be
	 *            compared to the <code>localFileRoot</code> The file and
	 *            collection names will be compared relative to the respective
	 *            left hand and right hand root absolute paths
	 * @param timestampForIrodsFileThatIndicatesThatTheFileHasChanged
	 *            <code>long</code> with the timestamp that, if before the last
	 *            modified date of the given left hand and right hand files,
	 *            indicates that the file has changed. Leave as zero to turn off
	 *            this check.
	 * @return {@link FileTreeModel} with the common directory structure and any
	 *         detected diffs. The model is made of {@link FileTreeNode} objects
	 *         that contain the {@link FileTreeDiffEntry} as the user data
	 *         object.
	 * @throws JargonException
	 */
	public abstract FileTreeModel generateDiffLocalToIRODS(
			final File localFileRoot, final String irodsAbsolutePath,
			final long timestampForIrodsFileThatIndicatesThatTheFileHasChanged)
			throws JargonException;

}