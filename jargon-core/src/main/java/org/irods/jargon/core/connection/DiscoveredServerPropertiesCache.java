/**
 * 
 */
package org.irods.jargon.core.connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Somewhat experimental cache of discovered properties, these are aspects of
 * iRODS servers (such as, whether specific query support is available), that
 * are discovered as a result of calling a function in Jargon. Instead of trying
 * and failing to get a certain service from iRODS over and over again, a result
 * can be cached here to check.
 * <p/>
 * This is a map of maps, the cache is by concatenated host + zone name. For
 * each host+zone, it will contain a map of plain <code>String</code> name/value
 * pairs.
 * <p/>
 * Note that we'll this using a modest {@link ConcurrentHashMap} implementation,
 * so that synch overhead is minimized. Note that the worst case side effect
 * would be asking iRODS for something (e.g. trying to run a specific query)
 * more than once, so I'm trying to minimize synchronization. We can allow some
 * 'fuzziness' here. The point is to minimize such redundant calls.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DiscoveredServerPropertiesCache {

	private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> discoveredServerPropertiesCache = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>(
			8, 0.9f, 1);

	/*
	 * basic properties that can be cached
	 */
	public static final String JARGON_SPECIFIC_QUERIES_SUPPORTED = "jargonSpecificQueriesSupported";

	public static final String IS_TRUE = "true";
	public static final String IS_FALSE = "false";

	public DiscoveredServerPropertiesCache() {
	}

	/**
	 * Get the value cached for this host and zone. Note that if the zone does
	 * not exist in the cache, or if the property does not exist in the zone
	 * cache, a <code>null</code> will be returned.
	 * 
	 * @param host
	 *            <code>String</code> with the name of the iRODS host this
	 *            applies to
	 * @param zoneName
	 *            <code>String</code> with the name of the iRODS zone this
	 *            applies to
	 * @param propertyName
	 *            <code>String</code> with the property value to be retrieved
	 * @return <code>String</code> with the value for the given property, or
	 *         <code>null</code> if no such value exists
	 */
	public String retrieveValue(final String host, final String zoneName,
			final String propertyName) {

		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("null or empty host");
		}

		if (zoneName == null || zoneName.isEmpty()) {
			throw new IllegalArgumentException("zoneName is null or empty");
		}

		if (propertyName == null || propertyName.isEmpty()) {
			throw new IllegalArgumentException("null or empty property name");
		}

		Map<String, String> zoneCache = getCacheForHostAndZone(host, zoneName);

		if (zoneCache == null) {
			return null;
		} else {
			return zoneCache.get(propertyName);
		}

	}

	/**
	 * Delete the property from the cache if it exists. If the zone has no
	 * cache, or the property itself is not cached, silently ignore
	 * 
	 * @param host
	 * @param zoneName
	 * @param propertyName
	 */
	public void deleteCachedProperty(final String host, final String zoneName,
			final String propertyName) {

		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("null or empty host");
		}

		if (zoneName == null || zoneName.isEmpty()) {
			throw new IllegalArgumentException("zoneName is null or empty");
		}

		if (propertyName == null || propertyName.isEmpty()) {
			throw new IllegalArgumentException("null or empty property name");
		}

		Map<String, String> zoneCache = getCacheForHostAndZone(host, zoneName);
		if (zoneCache != null) {
			zoneCache.remove(propertyName);
		}
	}

	/**
	 * Delete all cached props for the host and zone. If there is no zone cache,
	 * ignore the request
	 * 
	 * @param host
	 * @param zoneName
	 */
	public void deleteCache(final String host, final String zoneName) {

		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("null or empty host");
		}

		if (zoneName == null || zoneName.isEmpty()) {
			throw new IllegalArgumentException("zoneName is null or empty");
		}

		String cacheKey = buildHostPlusZone(host, zoneName);
		discoveredServerPropertiesCache.remove(cacheKey);

	}

	/**
	 * Cache a property for the given host and zone.
	 * 
	 * @param host
	 *            <code>String</code> with the name of the iRODS host this
	 *            applies to
	 * @param zoneName
	 *            <code>String</code> with the name of the iRODS zone this
	 *            applies to
	 * @param propertyName
	 *            <code>String</code> with the property to set
	 * @param value
	 *            <code>String</code> with a non-null value to set. For
	 *            consistency, set to blank if a property has no value.
	 */
	public void cacheAProperty(final String host, final String zoneName,
			final String propertyName, final String value) {

		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("null or empty host");
		}

		if (zoneName == null || zoneName.isEmpty()) {
			throw new IllegalArgumentException("zoneName is null or empty");
		}

		if (propertyName == null || propertyName.isEmpty()) {
			throw new IllegalArgumentException("null or empty property name");
		}

		if (value == null) {
			throw new IllegalArgumentException("null value");
		}

		getCacheForHostAndZoneAndAddIfNotThere(host, zoneName).put(
				propertyName, value);

	}

	/**
	 * Look for a properties map for the given host and zone, add to the main
	 * cache if it doesn't exist
	 * 
	 * @param host
	 * @param zoneName
	 * @return
	 */
	private Map<String, String> getCacheForHostAndZoneAndAddIfNotThere(
			final String host, final String zoneName) {

		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("null or empty host");
		}

		if (zoneName == null || zoneName.isEmpty()) {
			throw new IllegalArgumentException("zoneName is null or empty");
		}

		String cacheKey = buildHostPlusZone(host, zoneName);
		discoveredServerPropertiesCache.putIfAbsent(cacheKey,
				new ConcurrentHashMap<String, String>(8, 0.9f, 1));
		return discoveredServerPropertiesCache.get(cacheKey);
	}

	/**
	 * Look for a properties map for the given host and zone
	 * 
	 * @param host
	 * @param zoneName
	 * @return
	 */
	private Map<String, String> getCacheForHostAndZone(final String host,
			final String zoneName) {

		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("null or empty host");
		}

		if (zoneName == null || zoneName.isEmpty()) {
			throw new IllegalArgumentException("zoneName is null or empty");
		}

		String cacheKey = buildHostPlusZone(host, zoneName);
		return discoveredServerPropertiesCache.get(cacheKey);
	}

	/**
	 * Standard way to concatenate the host and zone name, trimming white space
	 * in the process. This allows consistent look up
	 * 
	 * @param host
	 * @param zoneName
	 * @return
	 */
	private String buildHostPlusZone(final String host, final String zoneName) {
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("null or empty host");
		}

		if (zoneName == null || zoneName.isEmpty()) {
			throw new IllegalArgumentException("zoneName is null or empty");
		}

		StringBuilder sb = new StringBuilder();
		sb.append(host.trim());
		sb.append(zoneName.trim());
		return sb.toString();
	}

}
