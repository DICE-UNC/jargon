package org.irods.jargon.datautils.datacache;

/**
 * Configuration value object for cache service
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class CacheServiceConfiguration {
	
	/**
	 * Clean up old cache files during each request (might create a slight response delay for large caches.
	 */
	private boolean doCleanupDuringRequests = true;
	
	/**
	 * Cache is created per user home dir
	 */
	private boolean cacheInHomeDir = true;
	
	/**
	 * Lifetime of cached data for determining purge
	 */
	private int lifetimeInMinutes = 10;
	
	public int getLifetimeInMinutes() {
		return lifetimeInMinutes;
	}

	public void setLifetimeInMinutes(int lifetimeInMinutes) {
		this.lifetimeInMinutes = lifetimeInMinutes;
	}

	/**
	 * Directory path for cache files.  If caching in home directory, this is a relative path under home.  If cacheInHomeDir is <code>false</code>, then
	 * path is an absolute path to the desired cache dir.
	 */
	private String cacheDirPath = "cacheServiceTempDir";
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CacheServiceConfiguration");
		sb.append("\n    doCleanupDuringRequests:");
		sb.append(doCleanupDuringRequests);
		sb.append("\n    cacheInHomeDir:");
		sb.append(cacheInHomeDir);
		sb.append("\n    cacheDirPath:");
		sb.append(cacheDirPath);
		return sb.toString();
	}
	
	public void setDoCleanupDuringRequests(boolean doCleanupDuringRequests) {
		this.doCleanupDuringRequests = doCleanupDuringRequests;
	}

	public boolean isDoCleanupDuringRequests() {
		return doCleanupDuringRequests;
	}

	/**
	 * @return the cacheInHomeDir
	 */
	public boolean isCacheInHomeDir() {
		return cacheInHomeDir;
	}

	/**
	 * @param cacheInHomeDir the cacheInHomeDir to set
	 */
	public void setCacheInHomeDir(boolean cacheInHomeDir) {
		this.cacheInHomeDir = cacheInHomeDir;
	}

	/**
	 * @return the cacheDirPath
	 */
	public String getCacheDirPath() {
		return cacheDirPath;
	}

	/**
	 * @param cacheDirPath the cacheDirPath to set
	 */
	public void setCacheDirPath(String cacheDirPath) {
		this.cacheDirPath = cacheDirPath;
	}
	
	
	
	

}
