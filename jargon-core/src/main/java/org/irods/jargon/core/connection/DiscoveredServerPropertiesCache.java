/**
 * 
 */
package org.irods.jargon.core.connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.irods.jargon.core.pub.SpecificQueryAOImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <p/>
 * This class also includes other cacheable data, such as the <code>IRODSServerProperties</code> that is otherwise repeatedly 
 * obtained from iRODS on connection
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DiscoveredServerPropertiesCache {

	private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> discoveredServerPropertiesCache = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>(
			8, 0.9f, 1);
	private ConcurrentHashMap<String, IRODSServerProperties> cacheOfIRODSServerProperties = new ConcurrentHashMap<String, IRODSServerProperties>(
			8, 0.9f, 1);
	
	public static final Logger log = LoggerFactory
			.getLogger(DiscoveredServerPropertiesCache.class);

	/*
	 * basic properties that can be cached
	 */
	public static final String JARGON_SPECIFIC_QUERIES_SUPPORTED = "jargonSpecificQueriesSupported";

	public static final String IS_TRUE = "true";
	public static final String IS_FALSE = "false";

	public DiscoveredServerPropertiesCache() {
	}
	
	/**
	 * 
	 * If an <code>IRODSServerProperties</code> was already cached, then just return it, if not cached, this method will return null
	 * @param host
	 *            <code>String</code> with the name of the iRODS host this
	 *            applies to
	 * @param zoneName
	 *            <code>String</code> with the name of the iRODS zone this
	 *            applies to
	 * @return {@link IRODSServerProperties} or <ocde>null</code> if not cached
	 */
	public IRODSServerProperties retrieveIRODSServerProperties(final String host, final String zoneName) {
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("null or empty host");
		}
		
		String myZone = zoneName;

		if (zoneName == null) {
			myZone = "";
		}
		
		log.info("now retriving server properties from cache with zone:{}", myZone);
		
		return getIRODSServerPropertiesForHostAndZone(host, myZone);
	}
	
	/**
	 * Gets the cached <code>IRODSServerProperties</code> or <code>null</code>
	 * @param host
	 * @param zoneName
	 * @return
	 */
	private IRODSServerProperties getIRODSServerPropertiesForHostAndZone(
			String host, String zoneName) {
		
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("null or empty host");
		}

		String myZone = zoneName;

		if (zoneName == null ) {
			myZone = "";
		}

		String cacheKey = buildHostPlusZone(host, myZone);
		return cacheOfIRODSServerProperties.get(cacheKey);
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

		String myZone = zoneName;

		if (zoneName == null ) {
			myZone = "";
		}

		if (propertyName == null || propertyName.isEmpty()) {
			throw new IllegalArgumentException("null or empty property name");
		}

		Map<String, String> zoneCache = getCacheForHostAndZone(host, myZone);

		if (zoneCache == null) {
			return null;
		} else {
			return zoneCache.get(propertyName);
		}

	}

	/**
	 * Delete the <code>IRODSServerProperties</code> If the zone has no
	 * cache, silently ignore
	 * 
	 * @param host
	 * @param zoneName
	 * @param propertyName
	 */
	public void deleteCachedIRODSServerProperties(final String host, final String zoneName,
			final String propertyName) {

		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("null or empty host");
		}

		String myZone = zoneName;

		if (zoneName == null) {
			myZone = "";
		}
		
		String cacheKey = buildHostPlusZone(host, myZone);
		cacheOfIRODSServerProperties.remove(cacheKey);
		
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

		String myZone = zoneName;

		if (zoneName == null) {
			myZone = "";
		}

		if (propertyName == null || propertyName.isEmpty()) {
			throw new IllegalArgumentException("null or empty property name");
		}

		Map<String, String> zoneCache = getCacheForHostAndZone(host, myZone);
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

		String myZone = zoneName;

		if (zoneName == null) {
			myZone = "";
		}
		
		String cacheKey = buildHostPlusZone(host, myZone);
		discoveredServerPropertiesCache.remove(cacheKey);

	}
	
	/**
	 * Add an <code>IRODSServerProperties</code> to the cache
	 * @param host
	 *            <code>String</code> with the name of the iRODS host this
	 *            applies to
	 * @param zoneName
	 *            <code>String</code> with the name of the iRODS zone this
	 *            applies to
	 * @param irodsServerProperties {@link IRODSServerProperties} to cache
	 */
	public void cacheIRODSServerProperties(final String host, final String zoneName, final IRODSServerProperties irodsServerProperties) {
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("null or empty host");
		}

		String myZone = zoneName;

		if (zoneName == null) {
			myZone = "";
		}
		
		if (irodsServerProperties == null) {
			throw new IllegalArgumentException("null irodsServerProperties");
		}
		
		String cacheKey = buildHostPlusZone(host, myZone);

		cacheOfIRODSServerProperties.put(cacheKey, irodsServerProperties);
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

		String myZone = zoneName;

		if (zoneName == null) {
			myZone = "";
		}
		
		if (propertyName == null || propertyName.isEmpty()) {
			throw new IllegalArgumentException("null or empty property name");
		}

		if (value == null) {
			throw new IllegalArgumentException("null value");
		}

		getCacheForHostAndZoneAndAddIfNotThere(host, myZone).put(
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

		String myZone = zoneName;

		if (zoneName == null) {
			myZone = "";
		}
		
		String cacheKey = buildHostPlusZone(host, myZone);
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

		String myZone = zoneName;

		if (zoneName == null) {
			myZone = "";
		}
		
		String cacheKey = buildHostPlusZone(host, myZone);
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

		String myZone = zoneName;

		if (zoneName == null) {
			myZone = "";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(host.trim());
		sb.append(myZone);
		return sb.toString();
	}

}
