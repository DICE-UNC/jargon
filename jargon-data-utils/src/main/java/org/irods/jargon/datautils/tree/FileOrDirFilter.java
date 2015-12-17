package org.irods.jargon.datautils.tree;

import java.io.File;
import java.io.FileFilter;

/**
 * Filter to discriminate between files and dirs for matching. This is necessary
 * because Data Objects and Collections may be returned in differing orders
 * between iRODS and the local file system.
 * <p/>
 * iRODS will return all the files, then all the directories. Local file systems
 * typically will have them mixed together.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class FileOrDirFilter implements FileFilter {

	public enum FilterFor {
		FILE, DIR
	}

	private final FilterFor filterFor;

	public FileOrDirFilter(final FilterFor filterFor) {
		if (filterFor == null) {
			throw new IllegalArgumentException("null filterFor");
		}
		this.filterFor = filterFor;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(final File file) {
		boolean show = false;

		if (filterFor == FilterFor.DIR) {
			if (file.isDirectory()) {
				show = true;
			} else {
				show = false;
			}
		} else {
			if (file.isDirectory()) {
				show = false;
			} else {
				show = true;
			}
		}

		return show;

	}

}
