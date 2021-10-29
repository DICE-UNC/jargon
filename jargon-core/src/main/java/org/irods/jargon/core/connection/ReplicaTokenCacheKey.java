/**
 * 
 */
package org.irods.jargon.core.connection;

/**
 * Compound key for the replica token cache
 * 
 * @author conwaymc
 *
 */
public class ReplicaTokenCacheKey {

	private final String combinedKey;

	/**
	 * Static instance method
	 * 
	 * @param irodsAbsolutePath {@code String} with the iRODS absolutePath
	 * @param userName          {@code String} with the iRODS user name
	 * @return {@link ReplicaTokenCacheKey} instance
	 */
	public static final ReplicaTokenCacheKey instance(final String irodsAbsolutePath, final String userName) {
		return new ReplicaTokenCacheKey(irodsAbsolutePath, userName);
	}

	private ReplicaTokenCacheKey(final String irodsAbsolutePath, final String userName) {

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		StringBuilder sb = new StringBuilder();
		sb.append(userName);
		sb.append('+');
		sb.append(irodsAbsolutePath);
		this.combinedKey = sb.toString();

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((combinedKey == null) ? 0 : combinedKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReplicaTokenCacheKey other = (ReplicaTokenCacheKey) obj;
		if (combinedKey == null) {
			if (other.combinedKey != null)
				return false;
		} else if (!combinedKey.equals(other.combinedKey))
			return false;
		return true;
	}

}
