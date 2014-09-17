/**
 * 
 */
package org.irods.jargon.datautils.tree;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.irods.jargon.core.utils.LocalFileUtils;

/**
 * POJO that describes a File tree and its characteristics
 * 
 * @author Mike Conway
 * 
 */
public class TreeSummary {

	public static final String SIZE_10K = "10K";
	public static final String SIZE_100K = "100K";
	public static final String SIZE_1M = "1M";
	public static final String SIZE_32M = "32M";
	public static final String SIZE_100M = "100M";
	public static final String SIZE_1GB = "1GB";
	public static final String SIZE_10GB = "10GB";
	public static final String SIZE_100GB = "100GB";
	public static final String SIZE_GT_100GB = "> 100 GB";

	private ConcurrentMap<String, AtomicLong> fileSizeSummaryMap = new ConcurrentHashMap<String, AtomicLong>();
	private final ConcurrentMap<String, AtomicLong> fileExtensionSummaryMap = new ConcurrentHashMap<String, AtomicLong>();

	private long totalBytes = 0L;
	private long totalFiles = 0L;
	private long minLength = 0L;
	private long maxLength = 0L;

	public TreeSummary() {

		// init file size summary
		fileSizeSummaryMap.put(SIZE_10K, new AtomicLong(0L));
		fileSizeSummaryMap.put(SIZE_100K, new AtomicLong(0L));
		fileSizeSummaryMap.put(SIZE_1M, new AtomicLong(0L));
		fileSizeSummaryMap.put(SIZE_32M, new AtomicLong(0L));
		fileSizeSummaryMap.put(SIZE_100M, new AtomicLong(0L));
		fileSizeSummaryMap.put(SIZE_1GB, new AtomicLong(0L));
		fileSizeSummaryMap.put(SIZE_10GB, new AtomicLong(0L));
		fileSizeSummaryMap.put(SIZE_100GB, new AtomicLong(0L));
		fileSizeSummaryMap.put(SIZE_GT_100GB, new AtomicLong(0L));

	}

	public long calculateAverageLength() {
		if (totalFiles == 0) {
			return 0L;
		}
		return totalBytes / totalFiles;
	}

	public void processFileInfo(File file) {

		long length = file.length();
		String extension = LocalFileUtils.getFileExtension(file.getName()
				.toLowerCase());

		totalBytes += length;
		totalFiles++;

		if (minLength == 0) {
			minLength = length;
		}

		if (length > maxLength) {
			maxLength = length;
		}

		fileExtensionSummaryMap.putIfAbsent(extension, new AtomicLong(0));
		fileExtensionSummaryMap.get(extension).incrementAndGet();

		// classify length
		if (length < 10 * 1024) {
			fileSizeSummaryMap.get(SIZE_10K).incrementAndGet();
		} else if (length < 100 * 1024) {
			fileSizeSummaryMap.get(SIZE_100K).incrementAndGet();
		} else if (length < 1 * 1024 * 1024) {
			fileSizeSummaryMap.get(SIZE_1M).incrementAndGet();
		} else if (length < 32 * 1024 * 1024) {
			fileSizeSummaryMap.get(SIZE_32M).incrementAndGet();
		} else if (length < 100 * 1024 * 1024) {
			fileSizeSummaryMap.get(SIZE_100M).incrementAndGet();
		} else if (length < 1 * 1024 * 1024 * 1024) {
			fileSizeSummaryMap.get(SIZE_1GB).incrementAndGet();
		} else if (length < 10 * 1024 * 1024) {
			fileSizeSummaryMap.get(SIZE_10GB).incrementAndGet();
		} else if (length < 100 * 1024 * 1024 * 1024) {
			fileSizeSummaryMap.get(SIZE_100GB).incrementAndGet();
		} else {
			fileSizeSummaryMap.get(SIZE_GT_100GB).incrementAndGet();
		}

	}

	/**
	 * @return the fileSizeSummaryMap
	 */
	public ConcurrentMap<String, AtomicLong> getFileSizeSummaryMap() {
		return fileSizeSummaryMap;
	}

	/**
	 * @param fileSizeSummaryMap
	 *            the fileSizeSummaryMap to set
	 */
	public void setFileSizeSummaryMap(
			ConcurrentMap<String, AtomicLong> fileSizeSummaryMap) {
		this.fileSizeSummaryMap = fileSizeSummaryMap;
	}

	/**
	 * @return the fileExtensionSummaryMap
	 */
	public ConcurrentMap<String, AtomicLong> getFileExtensionSummaryMap() {
		return fileExtensionSummaryMap;
	}

	/**
	 * @return the totalBytes
	 */
	public long getTotalBytes() {
		return totalBytes;
	}

	/**
	 * @return the totalFiles
	 */
	public long getTotalFiles() {
		return totalFiles;
	}

	/**
	 * @return the size10k
	 */
	public static String getSize10k() {
		return SIZE_10K;
	}

	/**
	 * @return the size100k
	 */
	public static String getSize100k() {
		return SIZE_100K;
	}

	/**
	 * @return the size1m
	 */
	public static String getSize1m() {
		return SIZE_1M;
	}

	/**
	 * @return the size32m
	 */
	public static String getSize32m() {
		return SIZE_32M;
	}

	/**
	 * @return the size100m
	 */
	public static String getSize100m() {
		return SIZE_100M;
	}

	/**
	 * @return the size1gb
	 */
	public static String getSize1gb() {
		return SIZE_1GB;
	}

	/**
	 * @return the size10gb
	 */
	public static String getSize10gb() {
		return SIZE_10GB;
	}

	/**
	 * @return the size100gb
	 */
	public static String getSize100gb() {
		return SIZE_100GB;
	}

	/**
	 * @return the sizeGt100gb
	 */
	public static String getSizeGt100gb() {
		return SIZE_GT_100GB;
	}

	/**
	 * @return the minLength
	 */
	public long getMinLength() {
		return minLength;
	}

	/**
	 * @return the maxLength
	 */
	public long getMaxLength() {
		return maxLength;
	}

}
